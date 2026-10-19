package ruiseki.storagecore.client.gui.widget.setting;

import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.StoragePanel;
import ruiseki.storagecore.client.gui.widget.StorageSettingPanel;
import ruiseki.storagecore.client.gui.widget.Tab;

public class SortingSettingTab extends SettingTab {

    public static final int[] SLOT_COLORS = { 0x7FFF00, // Chartreuse (default)
        0xFF0000, // Red
        0x00FF00, // Green
        0x0000FF, // Blue
        0xFFFF00, // Yellow
        0xFF00FF, // Magenta
        0x00FFFF, // Cyan
        0xFF8000, // Orange
        0x8000FF, // Purple
        0x0080FF, // Sky Blue
        0xFF0080, // Rose
        0x80FF00, // Lime
        0x00FF80, // Spring Green
        0xFF8080, // Light Red
        0x80FF80, // Light Green
        0x8080FF, // Light Blue
    };

    private int currentColorIndex;

    public SortingSettingTab(IStorageWrapper storageWrapper, StoragePanel storagePanel,
        StorageSettingPanel settingPanel, Tab parentTab) {
        super(
            2,
            OKBGuiTextures.NO_SORT_ICON,
            "gui.storage.sorting_settings",
            100,
            Tab.ExpandDirection.RIGHT,
            storageWrapper,
            storagePanel,
            settingPanel,
            parentTab);
    }

    public int getCurrentColorIndex() {
        return currentColorIndex;
    }
}
