package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.wrapper.IXpPumpUpgrade.XpPumpDirection;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget.Variant;
import ruiseki.okbackpack.common.item.pump.xp.XpHelpers;
import ruiseki.okbackpack.common.item.pump.xp.XpPumpUpgradeWrapper;
import ruiseki.okbackpack.compat.Mods;

public class XpPumpUpgradeWidget extends ExpandedUpgradeTabWidget<XpPumpUpgradeWrapper> {

    private static final List<Variant> DIRECTION_VARIANTS = Arrays.asList(
        new Variant(IKey.lang("gui.backpack.xp_pump.direction_input"), OKBGuiTextures.PUMP_INPUT_ICON),
        new Variant(IKey.lang("gui.backpack.xp_pump.direction_output"), OKBGuiTextures.XP_PUMP_OUTPUT_ICON),
        new Variant(IKey.lang("gui.backpack.xp_pump.direction_keep"), OKBGuiTextures.XP_PUMP_KEEP_ICON),
        new Variant(IKey.lang("gui.backpack.xp_pump.direction_off"), OKBGuiTextures.XP_PUMP_OFF_ICON));

    private static final List<Variant> MENDING_VARIANTS = Arrays.asList(
        new Variant(IKey.lang("gui.backpack.xp_pump.mending_on"), OKBGuiTextures.XP_PUMP_MEND_ICON),
        new Variant(IKey.lang("gui.backpack.xp_pump.mending_off"), OKBGuiTextures.XP_PUMP_NO_MEND_ICON));

    protected final XpPumpUpgradeWrapper wrapper;

    public XpPumpUpgradeWidget(int slotIndex, XpPumpUpgradeWrapper wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey) {
        super(slotIndex, 3, stack, panel, titleKey, 95);
        height(100);
        this.wrapper = wrapper;

        Flow firstRow = Flow.row()
            .coverChildren()
            .pos(8, 28)
            .childPadding(0);
        firstRow.child(buildDirectionButton());
        firstRow.child(buildLevelTargetField());
        child(firstRow);

        boolean mendingRow = Mods.EtFuturum.isModLoaded() && XpHelpers.isMendingEnabled();
        int actionY = 28 + 20 + 3;
        if (mendingRow) {
            Flow mendRow = Flow.row()
                .coverChildren()
                .pos(8, actionY)
                .childPadding(0);
            mendRow.child(buildMendingButton());
            child(mendRow);
            actionY += 20 + 3;
        }

        Flow actionRow = Flow.row()
            .coverChildren()
            .pos(8, actionY)
            .childPadding(0);
        actionRow.child(
            buildPlainActionButton(
                UpgradeSlotSHRegisters.UPDATE_XP_PUMP_GIVE_ALL,
                OKBGuiTextures.XP_PUMP_TAKE_ALL_ICON,
                "gui.backpack.xp_pump.take_all"));
        actionRow.child(buildTakeLevelsButton());
        actionRow.child(buildStoreLevelsButton());
        actionRow.child(
            buildPlainActionButton(
                UpgradeSlotSHRegisters.UPDATE_XP_PUMP_TAKE_ALL,
                OKBGuiTextures.XP_PUMP_STORE_ALL_ICON,
                "gui.backpack.xp_pump.store_all"));
        child(actionRow);
    }

    @Override
    protected XpPumpUpgradeWrapper getWrapper() {
        return wrapper;
    }

    protected CyclicVariantButtonWidget buildDirectionButton() {
        return new CyclicVariantButtonWidget(
            DIRECTION_VARIANTS,
            wrapper.getDirection()
                .ordinal(),
            index -> {
                XpPumpDirection direction = XpPumpDirection.values()[index];
                wrapper.setDirection(direction);
                if (getSlotSyncHandler() != null) {
                    getSlotSyncHandler().syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_XP_PUMP_DIRECTION),
                        buf -> NetworkUtils.writeEnumValue(buf, direction));
                }
            });
    }

    protected CyclicVariantButtonWidget buildMendingButton() {
        return new CyclicVariantButtonWidget(MENDING_VARIANTS, wrapper.isMending() ? 0 : 1, index -> {
            boolean enabled = index == 0;
            wrapper.setMending(enabled);
            if (getSlotSyncHandler() != null) {
                getSlotSyncHandler().syncToServer(
                    UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_XP_PUMP_MENDING),
                    buf -> buf.writeBoolean(enabled));
            }
        });
    }

    /**
     * Anvil-style scroll field showing "{level}级" (number white, unit gray) glued right next to
     * the direction button. Scroll wheel adjusts target level by 1 (or 10 with shift).
     */
    @SuppressWarnings({ "rawtypes" })
    protected Widget<?> buildLevelTargetField() {
        ButtonWidget<?> field = new ButtonWidget() {

            @Override
            public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
                int delta = (scrollDirection == UpOrDown.UP ? 1 : -1) * (Interactable.hasShiftDown() ? 10 : 1);
                int newTarget = Math.max(0, wrapper.getLevelTarget() + delta);
                wrapper.setLevelTarget(newTarget);
                if (getSlotSyncHandler() != null) {
                    getSlotSyncHandler().syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_XP_PUMP_LEVEL_TARGET),
                        buf -> buf.writeInt(newTarget));
                }
                return true;
            }
        };
        field.background(OKBGuiTextures.ANVIL_TEXT_FIELD_ENABLED);
        field.overlay(
            IKey.comp(
                IKey.dynamic(() -> String.valueOf(wrapper.getLevelTarget()))
                    .style(IKey.WHITE),
                IKey.lang("gui.backpack.xp_pump.level_unit")
                    .style(IKey.GRAY)));
        field.tooltipAutoUpdate(true);
        field.tooltipDynamic((RichTooltip tooltip) -> {
            tooltip.addLine(IKey.lang("gui.backpack.xp_pump.level_select.tooltip"));
            tooltip.addLine(
                IKey.lang("gui.backpack.scroll_to_change_value")
                    .style(IKey.GRAY, IKey.ITALIC));
        });
        field.size(60, 20);
        return field;
    }

    /**
     * Take-N-levels button: scroll wheel adjusts the level count, click pulls that many levels.
     * The number is drawn in green over the icon.
     */
    @SuppressWarnings({ "rawtypes" })
    protected ButtonWidget<?> buildTakeLevelsButton() {
        ButtonWidget<?> btn = new ButtonWidget() {

            @Override
            public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
                int delta = (scrollDirection == UpOrDown.UP ? 1 : -1) * (Interactable.hasShiftDown() ? 10 : 1);
                int next = Math.max(1, wrapper.getLevelsToTake() + delta);
                wrapper.setLevelsToTake(next);
                if (getSlotSyncHandler() != null) {
                    getSlotSyncHandler().syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_XP_PUMP_LEVELS_TO_TAKE),
                        buf -> buf.writeInt(next));
                }
                return true;
            }
        };
        btn.overlay(OKBGuiTextures.XP_PUMP_TAKE_LEVELS_ICON);
        btn.tooltipAutoUpdate(true);
        btn.tooltipDynamic((RichTooltip tooltip) -> {
            tooltip.addLine(
                IKey.lang("gui.backpack.xp_pump.take_levels", "\u00a7a" + wrapper.getLevelsToTake() + "\u00a7r"));
            tooltip.addLine(
                IKey.lang("gui.backpack.scroll_to_change_value")
                    .style(IKey.GRAY, IKey.ITALIC));
        });
        btn.onMousePressed(button -> {
            if (button == 0) {
                Interactable.playButtonClickSound();
                if (getSlotSyncHandler() != null) {
                    getSlotSyncHandler().syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_XP_PUMP_GIVE_LEVELS),
                        buf -> {});
                }
                return true;
            }
            return false;
        });
        btn.size(20, 20);
        return btn;
    }

    @SuppressWarnings({ "rawtypes" })
    protected ButtonWidget<?> buildStoreLevelsButton() {
        ButtonWidget<?> btn = new ButtonWidget() {

            @Override
            public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
                int delta = (scrollDirection == UpOrDown.UP ? 1 : -1) * (Interactable.hasShiftDown() ? 10 : 1);
                int next = Math.max(1, wrapper.getLevelsToStore() + delta);
                wrapper.setLevelsToStore(next);
                if (getSlotSyncHandler() != null) {
                    getSlotSyncHandler().syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_XP_PUMP_LEVELS_TO_STORE),
                        buf -> buf.writeInt(next));
                }
                return true;
            }
        };
        btn.overlay(OKBGuiTextures.XP_PUMP_STORE_LEVELS_ICON);
        btn.tooltipAutoUpdate(true);
        btn.tooltipDynamic((RichTooltip tooltip) -> {
            tooltip.addLine(
                IKey.lang("gui.backpack.xp_pump.store_levels", "\u00a7c" + wrapper.getLevelsToStore() + "\u00a7r"));
            tooltip.addLine(
                IKey.lang("gui.backpack.scroll_to_change_value")
                    .style(IKey.GRAY, IKey.ITALIC));
        });
        btn.onMousePressed(button -> {
            if (button == 0) {
                Interactable.playButtonClickSound();
                if (getSlotSyncHandler() != null) {
                    getSlotSyncHandler().syncToServer(
                        UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_XP_PUMP_TAKE_LEVELS),
                        buf -> {});
                }
                return true;
            }
            return false;
        });
        btn.size(20, 20);
        return btn;
    }

    protected ButtonWidget<?> buildPlainActionButton(String registerKey, IDrawable icon, String tooltipKey) {
        return new ButtonWidget<>().size(20, 20)
            .overlay(icon)
            .tooltipDynamic(tooltip -> tooltip.addLine(IKey.lang(tooltipKey)))
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    if (getSlotSyncHandler() != null) {
                        getSlotSyncHandler().syncToServer(UpgradeSlotSH.getId(registerKey), buf -> {});
                    }
                    return true;
                }
                return false;
            });
    }
}
