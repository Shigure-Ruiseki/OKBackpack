package ruiseki.okstoragecore.upgrades.xppump;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.UpOrDown;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class XpPumpUpgradeTab extends ExpandedUpgradeTab<XpPumpUpgradeWrapper> {

    private static final UITexture BACKGROUND_TILE_TEXTURE = UITexture.builder()
        .location(Reference.MOD_ID, "gui/gui_controls")
        .imageSize(256, 256)
        .xy(29, 146, 66, 56)
        .adaptable(1)
        .tiled()
        .build();

    private static final List<CyclicVariantButtonWidget.Variant> DIRECTION_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.xp_pump_input"),
            OSCTextures.PUMP_INPUT),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.xp_pump_output"),
            OSCTextures.XP_PUMP_OUTPUT),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.xp_pump_off"),
            OSCTextures.XP_PUMP_OFF));

    private static final List<CyclicVariantButtonWidget.Variant> MENDING_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.mend_items"),
            OSCTextures.XP_PUMP_MENDING_ON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.do_not_mend_items"),
            OSCTextures.XP_PUMP_MENDING_OFF));

    public XpPumpUpgradeTab(int slotIndex, XpPumpUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel,
        String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    @Override
    protected Flow addUpgradeColumn() {
        return super.addUpgradeColumn().crossAxisAlignment(Alignment.CrossAxis.START)
            .child(this.addDirectionRow())
            .child(this.addMendingRow())
            .child(this.addXpTakeStoreRow());
    }

    protected Flow addDirectionRow() {
        return Flow.row()
            .crossAxisAlignment(Alignment.CrossAxis.START)
            .coverChildren()
            .child(this.addDirectionButton())
            .child(this.addXpControlWidget());
    }

    protected ButtonWidget<?> addDirectionButton() {
        return new CyclicVariantButtonWidget(
            DIRECTION_VARIANTS,
            this.getWrapper()
                .getDirection()
                .ordinal(),
            index -> {
                this.getWrapper()
                    .setDirection(AutomationDirection.fromOrdinal(index));
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.XP_PUMP_DIRECTION, buffer -> buffer.writeInt(index));
            });
    }

    protected XpControlWidget addXpControlWidget() {
        return new XpControlWidget(this).background(BACKGROUND_TILE_TEXTURE)
            .size(60, 20);
    }

    protected Flow addMendingRow() {
        return Flow.row()
            .crossAxisAlignment(Alignment.CrossAxis.START)
            .coverChildren()
            .child(this.addMendingButton());
    }

    protected ButtonWidget<?> addMendingButton() {
        return new CyclicVariantButtonWidget(
            MENDING_VARIANTS,
            this.getWrapper()
                .shouldMendItems() ? 0 : 1,
            index -> {
                this.getWrapper()
                    .setMendItems(index == 0);
                this.getSyncHandler()
                    .syncToServer(
                        UpgradeSlotSHRegistries.XP_PUMP_SHOULD_MEND,
                        buffer -> buffer.writeBoolean(index == 0));
            });
    }

    protected Flow addXpTakeStoreRow() {
        return Flow.row()
            .crossAxisAlignment(Alignment.CrossAxis.START)
            .coverChildren()
            .child(this.addTakeAllButton())
            .child(this.addTakeLevelsButton())
            .child(this.addStoreLevelsButton())
            .child(this.addStoreAllButton());
    }

    protected ButtonWidget<?> addTakeAllButton() {
        return new ButtonWidget<>().size(20)
            .overlay(OSCTextures.XP_PUMP_TAKE_ALL)
            .tooltipDynamic(
                tooltip -> { tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.take_all_experience")); })
            .tooltipAutoUpdate(true)
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                Interactable.playButtonClickSound();
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.XP_PUMP_TAKE_ALL);
                return true;
            });
    }

    protected ButtonWidget<?> addTakeLevelsButton() {
        return new ButtonWidget<>().size(20)
            .overlay(OSCTextures.XP_PUMP_TAKE_LEVELS)
            .tooltipDynamic(tooltip -> {
                tooltip.addLine(
                    IKey.lang(
                        "gui.okstoragecore.upgrades.buttons.take_levels",
                        this.getWrapper()
                            .getLevelsToTake()));
                tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.take_levels.controls"));
            })
            .tooltipAutoUpdate(true)
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                Interactable.playButtonClickSound();
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.XP_PUMP_TAKE_LEVELS);
                return true;
            })
            .onMouseScrolled((direction, amount) -> {
                int delta = direction == UpOrDown.UP ? 1 : -1;
                int levels = Math.max(
                    0,
                    this.getWrapper()
                        .getLevelsToTake() + delta * amount);
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.XP_PUMP_SET_TAKE_LEVELS, buffer -> buffer.writeInt(levels));
                return true;
            });
    }

    protected ButtonWidget<?> addStoreLevelsButton() {
        return new ButtonWidget<>().size(20)
            .overlay(OSCTextures.XP_PUMP_STORE_LEVELS)
            .tooltipDynamic(tooltip -> {
                tooltip.addLine(
                    IKey.lang(
                        "gui.okstoragecore.upgrades.buttons.store_levels",
                        this.getWrapper()
                            .getLevelsToStore()));
                tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.store_levels.controls"));
            })
            .tooltipAutoUpdate(true)
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                Interactable.playButtonClickSound();
                this.getWrapper()
                    .takeLevelsFromPlayer(this.getPlayer());
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.XP_PUMP_STORE_LEVELS);
                return true;
            })
            .onMouseScrolled((direction, amount) -> {
                int delta = direction == UpOrDown.UP ? 1 : -1;
                int levels = Math.max(
                    0,
                    this.getWrapper()
                        .getLevelsToStore() + delta * amount);
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.XP_PUMP_SET_STORE_LEVELS, buffer -> buffer.writeInt(levels));
                return true;
            });
    }

    protected ButtonWidget<?> addStoreAllButton() {
        return new ButtonWidget<>().size(20)
            .overlay(OSCTextures.XP_PUMP_STORE_ALL)
            .tooltipDynamic(
                tooltip -> { tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.store_all_experience")); })
            .tooltipAutoUpdate(true)
            .onMousePressed(mouse -> {
                if (mouse != 0) return false;
                Interactable.playButtonClickSound();
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.XP_PUMP_STORE_ALL);
                return true;
            });
    }

    private static class XpControlWidget extends TextWidget<XpControlWidget> implements Interactable {

        protected final XpPumpUpgradeTab tab;

        private XpControlWidget(XpPumpUpgradeTab tab) {
            super(
                IKey.dynamicKey(
                    () -> IKey.lang(
                        "gui.okstoragecore.upgrades.controls.xp_level_select",
                        tab.getWrapper()
                            .getLevel())));
            this.tab = tab;
            this.color(Color.WHITE.main);
            this.tooltip(tooltip -> {
                tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.controls.xp_level_select.tooltip"));
                tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.controls.xp_level_select.tooltip.controls"));
            });
            this.textAlign(Alignment.CENTER);
        }

        @Override
        public boolean onMouseScroll(UpOrDown scrollDirection, int amount) {
            int delta = scrollDirection == UpOrDown.UP ? 1 : -1;
            int levels = Math.max(
                0,
                this.tab.getWrapper()
                    .getLevel() + delta * amount);
            this.tab.getWrapper()
                .setLevel(levels);
            this.tab.getSyncHandler()
                .syncToServer(UpgradeSlotSHRegistries.XP_PUMP_LEVEL, buffer -> buffer.writeInt(levels));
            return true;
        }
    }
}
