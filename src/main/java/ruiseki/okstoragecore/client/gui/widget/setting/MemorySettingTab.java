package ruiseki.okstoragecore.client.gui.widget.setting;

import java.util.Arrays;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import cpw.mods.fml.common.Optional;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegisters;
import ruiseki.okstoragecore.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.StorageSettingPanel;
import ruiseki.okstoragecore.client.gui.widget.Tab;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;

public class MemorySettingTab extends SettingTab {

    private static final List<CyclicVariantButtonWidget.Variant> RESPECT_NBT_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.ignore_nbt"),
            OSCTextures.IGNORE_NBT),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.match_nbt"),
            OSCTextures.MATCH_NBT));

    public MemorySettingTab(IStorageWrapper storageWrapper, StoragePanel storagePanel, StorageSettingPanel settingPanel,
        Tab parentTab) {
        super(
            OSCTextures.BRAIN,
            "gui.okstoragecore.settings.memory",
            Tab.ExpandDirection.RIGHT,
            storageWrapper,
            storagePanel,
            settingPanel,
            parentTab);

        this.getPhantomTab()
            .getTabIcon()
            .tooltipDynamic(tooltip -> {
                tooltip.clearText();
                tooltip.addLine(IKey.lang("gui.okstoragecore.settings.memory.tooltip_open_detail"));
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            })
            .tooltipAutoUpdate(true);
    }

    @Override
    protected Flow addSettingsColumn() {
        return super.addSettingsColumn().child(
            Flow.row()
                .coverChildren()
                .child(this.addSelectAllButton())
                .child(this.addUnselectAllButton())
                .child(this.addRespectNBTButton()));
    }

    protected ButtonWidget<?> addSelectAllButton() {
        return new ButtonWidget<>().overlay(OSCTextures.ALL_FOUR_SLOT)
            .onMousePressed(button -> {
                if (button == 0) {
                    MemorySettingsCategory settings = this.getMemorySettings();
                    settings.selectSlots(
                        0,
                        this.getStorageWrapper()
                            .getInventoryHandler()
                            .getSlots());
                    this.getStoragePanel()
                        .getStorageSH()
                        .syncToServer(StorageSHRegisters.SET_MEMORY_ALL_SLOT);
                    refreshBogoSortingContext();
                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.okstoragecore.settings.buttons.select_all_slots"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));
    }

    protected ButtonWidget<?> addUnselectAllButton() {
        return new ButtonWidget<>().overlay(OSCTextures.NONE_FOUR_SLOT)
            .onMousePressed(button -> {
                if (button == 0) {
                    MemorySettingsCategory settings = this.getMemorySettings();
                    settings.unselectAllSlots();
                    this.getStoragePanel()
                        .getStorageSH()
                        .syncToServer(StorageSHRegisters.UNSET_MEMORY_ALL_SLOT);
                    refreshBogoSortingContext();
                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.okstoragecore.settings.buttons.unselect_all_slots"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));
    }

    protected ButtonWidget<?> addRespectNBTButton() {
        return new CyclicVariantButtonWidget(RESPECT_NBT_VARIANTS, index -> {
            this.getMemorySettings()
                .setIgnoreNbt(index != 0);
            this.getStoragePanel()
                .getStorageSH()
                .syncToServer(StorageSHRegisters.SET_IGNORE_NBT, buffer -> buffer.writeBoolean(index != 0));
            refreshBogoSortingContext();
        }).setIndex(
            this.getMemorySettings()
                .ignoresNbt() ? 1 : 0);
    }

    public MemorySettingsCategory getMemorySettings() {
        return this.getStorageWrapper()
            .getSettingsHandler()
            .getTypeCategory(MemorySettingsCategory.class);
    }

    @Optional.Method(modid = "bogosorter")
    private static void refreshBogoSortingContext() {
        // BackpackBogoSorterClientCompat.invalidateSortingContext();
    }
}
