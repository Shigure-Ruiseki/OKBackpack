package ruiseki.storagecore.client.gui.widget.setting;

import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.StoragePanel;
import ruiseki.storagecore.client.gui.widget.StorageSettingPanel;
import ruiseki.storagecore.client.gui.widget.Tab;

public class MemorySettingTab extends SettingTab {

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
    }
}
