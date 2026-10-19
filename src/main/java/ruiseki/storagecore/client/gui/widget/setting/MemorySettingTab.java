package ruiseki.storagecore.client.gui.widget.setting;

import java.util.Arrays;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.sync.StorageSHRegisters;
import ruiseki.storagecore.client.gui.widget.StoragePanel;
import ruiseki.storagecore.client.gui.widget.StorageSettingPanel;
import ruiseki.storagecore.client.gui.widget.Tab;
import ruiseki.storagecore.settings.memory.MemorySettingsCategory;

public class MemorySettingTab extends SettingTab {

    private static final List<CyclicVariantButtonWidget.Variant> RESPECT_NBT_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.storage.ignore_nbt"), OKBGuiTextures.IGNORE_NBT_ICON),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.storage.match_nbt"), OKBGuiTextures.MATCH_NBT_ICON));

    public MemorySettingTab(IStorageWrapper storageWrapper, StoragePanel storagePanel, StorageSettingPanel settingPanel,
        Tab parentTab) {
        super(
            2,
            OKBGuiTextures.BRAIN_ICON,
            "gui.storage.memory_settings",
            100,
            Tab.ExpandDirection.RIGHT,
            storageWrapper,
            storagePanel,
            settingPanel,
            parentTab);

        ButtonWidget<?> selectAllButton = new ButtonWidget<>().size(20)
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
                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.storage.select_all_slots"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        ButtonWidget<?> unselectAllButton = new ButtonWidget<>().size(20)
            .overlay(OKBGuiTextures.NONE_FOUR_SLOT_ICON)
            .onMousePressed(button -> {
                if (button == 0) {
                    MemorySettingsCategory settings = this.getMemorySettings();
                    settings.unselectAllSlots();
                    this.getStoragePanel()
                        .getStorageSH()
                        .syncToServer(StorageSHRegisters.UNSET_MEMORY_ALL_SLOT);

                    return true;
                }
                return false;
            })
            .tooltipStatic(
                t -> t.addLine(IKey.lang("gui.storage.unselect_all_slots"))
                    .pos(RichTooltip.Pos.NEXT_TO_MOUSE));

        CyclicVariantButtonWidget respectNBTButton = new CyclicVariantButtonWidget(RESPECT_NBT_VARIANTS, index -> {
            this.getMemorySettings()
                .setIgnoreNbt(index != 0);
            this.getStoragePanel()
                .getStorageSH()
                .syncToServer(StorageSHRegisters.SET_IGNORE_NBT, buffer -> buffer.writeBoolean(index != 0));
        });

        this.child(
            Flow.row()
                .leftRel(0.5f)
                .height(20)
                .top(28)
                .coverChildrenWidth()
                .childPadding(0)
                .child(selectAllButton)
                .child(unselectAllButton)
                .child(respectNBTButton));

        this.getPhantomTab()
            .getTabIcon()
            .tooltipDynamic(tooltip -> {
                tooltip.clearText();
                tooltip.addLine(IKey.lang("gui.storage.memory_settings.tooltip_open_detail"));
                tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
            })
            .tooltipAutoUpdate(true);
    }

    public MemorySettingsCategory getMemorySettings() {
        return this.getStorageWrapper()
            .getSettingsHandler()
            .getTypeCategory(MemorySettingsCategory.class);
    }
}
