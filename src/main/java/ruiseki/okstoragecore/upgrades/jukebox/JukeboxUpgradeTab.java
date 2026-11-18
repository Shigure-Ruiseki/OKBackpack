package ruiseki.okstoragecore.upgrades.jukebox;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetThemeEntry;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedBooleanSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedIntSH;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.StorageSlotGrid;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class JukeboxUpgradeTab extends ExpandedUpgradeTab<JukeboxUpgradeWrapper> {

    private final DelegatedIntSH activeSlotSH;
    private final DelegatedBooleanSH isPlayingSH;

    public JukeboxUpgradeTab(int slotIndex, JukeboxUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel,
        String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
        this.activeSlotSH = getSyncManager()
            .findSyncHandler("jukebox_slot_active_" + this.getSlotIndex(), DelegatedIntSH.class);
        this.isPlayingSH = getSyncManager()
            .findSyncHandler("jukebox_is_playing_" + this.getSlotIndex(), DelegatedBooleanSH.class);
    }

    @Override
    protected Flow addUpgradeColumn() {
        return super.addUpgradeColumn().child(this.addSlotGrid())
            .child(this.addButtonRow());
    }

    protected StorageSlotGrid<ItemSlot> addSlotGrid() {
        final int columns = 4;
        final int rows = (this.getWrapper()
            .getSlotCount() + columns
            - 1) / columns;

        return StorageSlotGrid.<ItemSlot>builder()
            .matrix(columns, rows, 'F')
            .maxSlots(
                this.getWrapper()
                    .getSlotCount())
            .key('F', i -> {
                ItemSlotSH slotSH = getSyncManager()
                    .findSyncHandler("jukebox_slot_" + this.getSlotIndex(), i, ItemSlotSH.class);
                slotSH.getSlot()
                    .changeListener((newItem, onlyAmountChanged, client, init) -> {
                        if (client && this.isPlayingSH.getValue() && this.activeSlotSH.getValue() == i) {
                            this.getSyncHandler()
                                .syncToServer(UpgradeSlotSHRegistries.JUKEBOX_STOP);
                        }
                    });
                return new ItemSlot() {

                    @Override
                    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
                        if (isPlayingSH.getValue() && activeSlotSH.getValue() == i) {
                            IDrawable.EMPTY.draw(context, 0, 0, 18, 18, widgetTheme.getTheme());
                            GuiDraw.drawRect(1, 1, 16, 16, 0x4400FF00);
                        }
                        super.draw(context, widgetTheme);
                    }

                }.name("jukebox_slot_" + this.getSlotIndex() + "_" + i)
                    .syncHandler(slotSH);
            })
            .build();
    }

    protected Flow addButtonRow() {
        return Flow.row()
            .coverChildren()
            .childPadding(2)
            .child(this.addStopButton())
            .child(this.addPlayButton());
    }

    protected ButtonWidget<?> addStopButton() {
        return new ButtonWidget<>().overlay(OSCTextures.STOP)
            .size(18, 18)
            .tooltipDynamic(tooltip -> tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.stop")))
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    this.getSyncHandler()
                        .syncToServer(UpgradeSlotSHRegistries.JUKEBOX_STOP);
                    return true;
                }
                return false;
            });
    }

    protected ButtonWidget<?> addPlayButton() {
        return new ButtonWidget<>().overlay(OSCTextures.PLAY)
            .size(18, 18)
            .tooltipDynamic(tooltip -> tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.play")))
            .onMousePressed(button -> {
                if (button == 0) {
                    Interactable.playButtonClickSound();
                    this.getSyncHandler()
                        .syncToServer(UpgradeSlotSHRegistries.JUKEBOX_PLAY);
                    return true;
                }
                return false;
            });
    }

    public static class Basic extends JukeboxUpgradeTab {

        public Basic(int slotIndex, JukeboxUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.jukebox");
        }
    }

    public static class Advanced extends JukeboxUpgradeTab {

        private static final List<CyclicVariantButtonWidget.Variant> SHUFFLE_VARIANTS = Arrays.asList(
            new CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.okstoragecore.upgrades.buttons.shuffle_on"),
                OSCTextures.SHUFFLE_ON),
            new CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.okstoragecore.upgrades.buttons.shuffle_off"),
                OSCTextures.SHUFFLE_OFF));

        private static final List<CyclicVariantButtonWidget.Variant> REPEAT_VARIANTS = Arrays.asList(
            new CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.okstoragecore.upgrades.buttons.repeat_all"),
                OSCTextures.REPEAT_ALL),
            new CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.okstoragecore.upgrades.buttons.repeat_one"),
                OSCTextures.REPEAT_ONE),
            new CyclicVariantButtonWidget.Variant(
                IKey.lang("gui.okstoragecore.upgrades.buttons.no_repeat"),
                OSCTextures.REPEAT_OFF));

        public Advanced(int slotIndex, JukeboxUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.advanced_jukebox");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn().child(this.addModeRow());
        }

        @Override
        protected Flow addButtonRow() {
            return super.addButtonRow().child(0, this.addPreviousButton())
                .child(this.addNextButton());
        }

        protected ButtonWidget<?> addPreviousButton() {
            return new ButtonWidget<>().overlay(OSCTextures.PREVIOUS)
                .size(18, 18)
                .tooltipDynamic(
                    tooltip -> tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.previous_disc")))
                .onMousePressed(button -> {
                    if (button == 0) {
                        Interactable.playButtonClickSound();
                        this.getSyncHandler()
                            .syncToServer(UpgradeSlotSHRegistries.JUKEBOX_PREVIOUS);
                        return true;
                    }
                    return false;
                });
        }

        protected ButtonWidget<?> addNextButton() {
            return new ButtonWidget<>().overlay(OSCTextures.NEXT)
                .size(18, 18)
                .tooltipDynamic(tooltip -> tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.next_disc")))
                .onMousePressed(button -> {
                    if (button == 0) {
                        Interactable.playButtonClickSound();
                        this.getSyncHandler()
                            .syncToServer(UpgradeSlotSHRegistries.JUKEBOX_NEXT);
                        return true;
                    }
                    return false;
                });
        }

        protected Flow addModeRow() {
            return Flow.row()
                .coverChildren()
                .childPadding(2)
                .child(this.addShuffleButton())
                .child(this.addRepeatButton());
        }

        protected ButtonWidget<?> addShuffleButton() {
            return new CyclicVariantButtonWidget(
                SHUFFLE_VARIANTS,
                this.getWrapper()
                    .isShuffleEnabled() ? 0 : 1,
                index -> {
                    this.getWrapper()
                        .setShuffleEnabled(index == 0);
                    this.getSyncHandler()
                        .syncToServer(
                            UpgradeSlotSHRegistries.JUKEBOX_SHUFFLE,
                            buffer -> buffer.writeBoolean(index == 0));
                });
        }

        protected ButtonWidget<?> addRepeatButton() {
            return new CyclicVariantButtonWidget(
                REPEAT_VARIANTS,
                this.getWrapper()
                    .getRepeatMode()
                    .ordinal(),
                index -> {
                    this.getWrapper()
                        .setRepeatMode(RepeatMode.fromOrdinal(index));
                    this.getSyncHandler()
                        .syncToServer(UpgradeSlotSHRegistries.JUKEBOX_REPEAT, buffer -> buffer.writeInt(index));
                });
        }
    }

}
