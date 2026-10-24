package ruiseki.okstoragecore.client.gui.widget.setting;

import java.util.Arrays;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import cpw.mods.fml.common.Optional;
import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.compat.bogosorter.BackpackBogoSorterClientCompat;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegisters;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.StorageSettingPanel;
import ruiseki.okstoragecore.client.gui.widget.Tab;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;

public class MemorySettingTab extends SettingTab {

    private static final List<CyclicVariantButtonWidget.Variant> RESPECT_NBT_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.ignore_nbt"),
            OKBGuiTextures.IGNORE_NBT_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.okstoragecore.match_nbt"), OKBGuiTextures.MATCH_NBT_ICON));

    public MemorySettingTab(IStorageWrapper storageWrapper, StoragePanel storagePanel, StorageSettingPanel settingPanel,
        Tab parentTab) {
        super(
            OKBGuiTextures.BRAIN_ICON,
            "gui.okstoragecore.memory_settings",
            Tab.ExpandDirection.RIGHT,
            storageWrapper,
            storagePanel,
            settingPanel,
            parentTab);

        this.getPhantomTab()
            .getTabIcon()
            .tooltipDynamic(tooltip -> {
                tooltip.clearText();
                tooltip.addLine(IKey.lang("gui.okstoragecore.memory_settings.tooltip_open_detail"));
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
        return new ButtonWidget<>().size(20)
            .overlay(OKBGuiTextures.ALL_FOUR_SLOT_ICON)
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
                t -> t.addLine(IKey.lang("gui.okstoragecore.select_all_slots"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));
    }

    protected ButtonWidget<?> addUnselectAllButton() {
        return new ButtonWidget<>().size(20)
            .overlay(OKBGuiTextures.NONE_FOUR_SLOT_ICON)
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
                t -> t.addLine(IKey.lang("gui.okstoragecore.unselect_all_slots"))
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
        BackpackBogoSorterClientCompat.invalidateSortingContext();
    }
}
