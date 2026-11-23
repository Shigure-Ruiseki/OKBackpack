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
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedBooleanSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedIntSH;
import ruiseki.okstoragecore.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.StorageSlotGrid;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class JukeboxUpgradeTab extends ExpandedUpgradeTab<JukeboxUpgradeWrapper> {

    public JukeboxUpgradeTab(int slotIndex, JukeboxUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel,
        String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    public static class Basic extends JukeboxUpgradeTab {

        public Basic(int slotIndex, JukeboxUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.jukebox");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn().child(
                new JukeboxControl(
                    this.getSlotIndex(),
                    this.getWrapper()
                        .getSlotCount(),
                    this.getWrapper(),
                    this,
                    "basic_jukebox_slot"));
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
            return super.addUpgradeColumn().child(
                new AdvancedJukeboxControl(
                    this.getSlotIndex(),
                    this.getWrapper()
                        .getSlotCount(),
                    this.getWrapper(),
                    this,
                    "adv_jukebox_slot"));
        }

        protected static class AdvancedJukeboxControl extends JukeboxControl {

            protected AdvancedJukeboxControl(int slotIndex, int slotCount, JukeboxUpgradeWrapper wrapper,
                ExpandedUpgradeTab<JukeboxUpgradeWrapper> tab, String syncKey) {
                super(slotIndex, slotCount, wrapper, tab, syncKey);
            }

            @Override
            protected Flow addContentRow() {
                return super.addContentRow().child(this.addModeRow());
            }

            @Override
            protected Flow addControlButtons() {
                return super.addControlButtons().child(0, this.addPreviousButton())
                    .child(this.addNextButton());
            }

            protected ButtonWidget<?> addPreviousButton() {
                return new ButtonWidget<>().overlay(OSCTextures.PREVIOUS)
                    .tooltipDynamic(
                        tooltip -> tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.previous_disc")))
                    .onMousePressed(button -> {
                        if (button == 0) {
                            Interactable.playButtonClickSound();
                            tab.getSyncHandler()
                                .syncToServer(UpgradeSlotSHRegistries.JUKEBOX_PREVIOUS);
                            return true;
                        }
                        return false;
                    });
            }

            protected ButtonWidget<?> addNextButton() {
                return new ButtonWidget<>().overlay(OSCTextures.NEXT)
                    .tooltipDynamic(
                        tooltip -> tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.next_disc")))
                    .onMousePressed(button -> {
                        if (button == 0) {
                            Interactable.playButtonClickSound();
                            tab.getSyncHandler()
                                .syncToServer(UpgradeSlotSHRegistries.JUKEBOX_NEXT);
                            return true;
                        }
                        return false;
                    });
            }

            protected Flow addModeRow() {
                return Flow.row()
                    .coverChildren()
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
                        tab.getSyncHandler()
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
                        tab.getSyncHandler()
                            .syncToServer(UpgradeSlotSHRegistries.JUKEBOX_REPEAT, buffer -> buffer.writeInt(index));
                    });
            }
        }

    }

    protected static class JukeboxControl extends ParentWidget<JukeboxControl> {

        protected final int slotIndex;
        protected final int slotCount;
        protected final ExpandedUpgradeTab<JukeboxUpgradeWrapper> tab;
        protected final JukeboxUpgradeWrapper wrapper;
        protected final String syncKey;

        protected final DelegatedIntSH activeSlotSH;
        protected final DelegatedBooleanSH isPlayingSH;

        protected JukeboxControl(int slotIndex, int slotCount, JukeboxUpgradeWrapper wrapper,
            ExpandedUpgradeTab<JukeboxUpgradeWrapper> tab, String syncKey) {
            this.slotIndex = slotIndex;
            this.tab = tab;
            this.wrapper = wrapper;
            this.slotCount = slotCount;
            this.syncKey = syncKey;

            this.activeSlotSH = tab.getSyncManager()
                .findSyncHandler("jukebox_slot_active_" + slotIndex, DelegatedIntSH.class);
            this.isPlayingSH = tab.getSyncManager()
                .findSyncHandler("jukebox_is_playing_" + slotIndex, DelegatedBooleanSH.class);

            this.coverChildren();
            this.child(this.addContentRow());
        }

        public JukeboxUpgradeWrapper getWrapper() {
            return wrapper;
        }

        public String getSyncKey() {
            return syncKey;
        }

        protected Flow addContentRow() {
            return Flow.column()
                .coverChildren()
                .childPadding(2)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .child(this.addSlotGrid())
                .child(this.addControlButtons());
        }

        protected StorageSlotGrid<ItemSlot> addSlotGrid() {
            final int columns = Math.clamp(slotCount, 1, 4);
            final int rows = Math.max(1, (slotCount + columns - 1) / columns);
            final String slotSyncKey = syncKey + "_" + slotIndex;

            return StorageSlotGrid.<ItemSlot>builder()
                .matrix(columns, rows, 'F')
                .maxSlots(slotCount)
                .key('F', i -> {
                    ItemSlotSH slotSH = tab.getSyncManager()
                        .findSyncHandler(slotSyncKey, i, ItemSlotSH.class);

                    slotSH.getSlot()
                        .changeListener((newItem, onlyAmountChanged, client, init) -> {
                            if (client && isPlayingSH.getValue() && activeSlotSH.getValue() == i) {
                                tab.getSyncHandler()
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
                    }.name(slotSyncKey + "_" + i)
                        .syncHandler(slotSH);
                })
                .build();
        }

        protected Flow addControlButtons() {
            return Flow.row()
                .coverChildren()
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .child(this.addStopButton())
                .child(this.addPlayButton());
        }

        protected ButtonWidget<?> addStopButton() {
            return new ButtonWidget<>().overlay(OSCTextures.STOP)
                .tooltipDynamic(tooltip -> tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.stop")))
                .onMousePressed(button -> {
                    if (button == 0) {
                        Interactable.playButtonClickSound();
                        tab.getSyncHandler()
                            .syncToServer(UpgradeSlotSHRegistries.JUKEBOX_STOP);
                        return true;
                    }
                    return false;
                });
        }

        protected ButtonWidget<?> addPlayButton() {
            return new ButtonWidget<>().overlay(OSCTextures.PLAY)
                .tooltipDynamic(tooltip -> tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.play")))
                .onMousePressed(button -> {
                    if (button == 0) {
                        Interactable.playButtonClickSound();
                        tab.getSyncHandler()
                            .syncToServer(UpgradeSlotSHRegistries.JUKEBOX_PLAY);
                        return true;
                    }
                    return false;
                });
        }
    }
}
