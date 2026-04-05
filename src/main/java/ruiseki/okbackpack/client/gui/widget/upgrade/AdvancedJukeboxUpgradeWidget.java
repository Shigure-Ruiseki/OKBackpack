package ruiseki.okbackpack.client.gui.widget.upgrade;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import lombok.Getter;
import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.wrapper.IJukeboxUpgrade.JukeboxLoopMode;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.UpgradeSlotSHRegisters;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.common.item.jukebox.AdvancedJukeboxUpgradeWrapper;

public class AdvancedJukeboxUpgradeWidget extends ExpandedUpgradeTabWidget<AdvancedJukeboxUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> SHUFFLE_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.jukebox_shuffle_off"),
            OKBGuiTextures.SHUFFLE_OFF_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.jukebox_shuffle_on"),
            OKBGuiTextures.SHUFFLE_ON_ICON));

    private static final List<CyclicVariantButtonWidget.Variant> LOOP_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.jukebox_loop_off"), OKBGuiTextures.LOOP_OFF_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.backpack.jukebox_loop_all"), OKBGuiTextures.LOOP_ALL_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.backpack.jukebox_loop_single"),
            OKBGuiTextures.LOOP_SINGLE_ICON));

    private final AdvancedJukeboxUpgradeWrapper wrapper;

    @Getter
    private final CyclicVariantButtonWidget shuffleButton;
    @Getter
    private final CyclicVariantButtonWidget loopButton;

    public AdvancedJukeboxUpgradeWidget(int slotIndex, AdvancedJukeboxUpgradeWrapper wrapper, ItemStack stack,
        IStoragePanel<?> panel, String titleKey) {
        super(slotIndex, 5, stack, titleKey, 100);
        this.wrapper = wrapper;

        SlotGroupWidget slotGroup = new SlotGroupWidget().name("adv_jukebox_slots")
            .coverChildren();

        for (int i = 0; i < 16; i++) {
            final int slotIdx = i;
            ItemSlot recordSlot = new ItemSlot() {

                @Override
                public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
                    if (wrapper.isPlaying() && wrapper.getCurrentSlotIndex() == slotIdx) {
                        IDrawable.EMPTY.draw(context, 0, 0, 18, 18, widgetTheme.getTheme());
                        GuiDraw.drawRect(1, 1, 16, 16, 0x4400FF00);
                    }
                    super.draw(context, widgetTheme);
                }
            };
            recordSlot.syncHandler("adv_jukebox_handler_" + slotIndex, i)
                .pos(i % 4 * 18, i / 4 * 18)
                .name("record_slot_" + i);
            slotGroup.child(recordSlot);
        }

        ButtonWidget<?> prevButton = new ButtonWidget<>().overlay(OKBGuiTextures.JUKEBOX_PREV_ICON)
            .size(18, 18)
            .tooltipDynamic(tooltip -> tooltip.addLine(IKey.lang("gui.backpack.jukebox_prev")))
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    if (!wrapper.isEnabled() || !wrapper.isPlaying()) return true;
                    wrapper.previous();
                    syncJukeboxAction(UpgradeSlotSHRegisters.UPDATE_JUKEBOX_PREV);
                    return true;
                }
                return false;
            });

        ButtonWidget<?> stopButton = new ButtonWidget<>().overlay(OKBGuiTextures.JUKEBOX_STOP_ICON)
            .size(18, 18)
            .tooltipDynamic(tooltip -> tooltip.addLine(IKey.lang("gui.backpack.jukebox_stop")))
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    wrapper.stop();
                    syncJukeboxAction(UpgradeSlotSHRegisters.UPDATE_JUKEBOX_STOP);
                    return true;
                }
                return false;
            });

        ButtonWidget<?> playButton = new ButtonWidget<>().overlay(OKBGuiTextures.JUKEBOX_PLAY_ICON)
            .size(18, 18)
            .tooltipDynamic(tooltip -> tooltip.addLine(IKey.lang("gui.backpack.jukebox_play")))
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    wrapper.play();
                    syncJukeboxAction(UpgradeSlotSHRegisters.UPDATE_JUKEBOX_PLAY);
                    return true;
                }
                return false;
            });

        ButtonWidget<?> nextButton = new ButtonWidget<>().overlay(OKBGuiTextures.JUKEBOX_NEXT_ICON)
            .size(18, 18)
            .tooltipDynamic(tooltip -> tooltip.addLine(IKey.lang("gui.backpack.jukebox_next")))
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    if (!wrapper.isEnabled() || !wrapper.isPlaying()) return true;
                    wrapper.next();
                    syncJukeboxAction(UpgradeSlotSHRegisters.UPDATE_JUKEBOX_NEXT);
                    return true;
                }
                return false;
            });

        Row transportRow = (Row) new Row().coverChildrenHeight()
            .leftRel(0.5f)
            .childPadding(0)
            .child(prevButton)
            .child(stopButton)
            .child(playButton)
            .child(nextButton);

        this.shuffleButton = new CyclicVariantButtonWidget(
            SHUFFLE_VARIANTS,
            wrapper.isShuffleEnabled() ? 1 : 0,
            index -> {
                wrapper.setShuffleEnabled(index == 1);
                syncJukeboxShuffle();
            }).size(18, 18)
                .pos(8, 0);

        this.loopButton = new CyclicVariantButtonWidget(
            LOOP_VARIANTS,
            wrapper.getLoopMode()
                .ordinal(),
            index -> {
                wrapper.setLoopMode(JukeboxLoopMode.values()[index]);
                syncJukeboxLoop();
            }).size(18, 18)
                .pos(44, 0);

        Row modeRow = (Row) new Row().coverChildrenHeight()
            .width(70)
            .leftRel(0.5f);
        modeRow.child(shuffleButton)
            .child(loopButton);

        Column column = (Column) new Column().pos(8, 28)
            .coverChildren()
            .childPadding(2)
            .child(slotGroup)
            .child(transportRow)
            .child(modeRow);

        child(column);
    }

    @Override
    protected AdvancedJukeboxUpgradeWrapper getWrapper() {
        return wrapper;
    }

    private void syncJukeboxAction(String action) {
        if (getSlotSyncHandler() != null) {
            getSlotSyncHandler().syncToServer(UpgradeSlotSH.getId(action), buf -> {});
        }
    }

    private void syncJukeboxShuffle() {
        if (getSlotSyncHandler() != null) {
            getSlotSyncHandler().syncToServer(
                UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_JUKEBOX_SHUFFLE),
                buf -> buf.writeBoolean(wrapper.isShuffleEnabled()));
        }
    }

    private void syncJukeboxLoop() {
        if (getSlotSyncHandler() != null) {
            getSlotSyncHandler().syncToServer(
                UpgradeSlotSH.getId(UpgradeSlotSHRegisters.UPDATE_JUKEBOX_LOOP),
                buf -> buf.writeInt(
                    wrapper.getLoopMode()
                        .ordinal()));
        }
    }
}
