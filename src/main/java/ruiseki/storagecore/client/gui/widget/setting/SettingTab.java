package ruiseki.storagecore.client.gui.widget.setting;

import com.cleanroommc.modularui.api.drawable.IDrawable;

import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.ExpandedTab;
import ruiseki.storagecore.client.gui.widget.StoragePanel;
import ruiseki.storagecore.client.gui.widget.StorageSettingPanel;
import ruiseki.storagecore.client.gui.widget.Tab;

public class SettingTab extends ExpandedTab {

    private final IStorageWrapper storageWrapper;
    private final StoragePanel storagePanel;
    private final StorageSettingPanel settingPanel;
    private final Tab parentTab;

    public SettingTab(int coveredTabSize, IDrawable delegatedIcon, String titleKey, int width,
        Tab.ExpandDirection expandDirection, IStorageWrapper storageWrapper, StoragePanel storagePanel,
        StorageSettingPanel settingPanel, Tab parentTab) {
        super(coveredTabSize, delegatedIcon, titleKey, width, expandDirection);
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
        this.settingPanel = settingPanel;
        this.parentTab = parentTab;
    }

    @Override
    public void updateTabState() {
        boolean shouldClose = this.getParentTab()
            .isShowExpanded();
        this.getSettingPanel()
            .resetTabState();
        if (!shouldClose) {
            this.getParentTab()
                .setShowExpanded(true);
            this.getSettingPanel()
                .getContext()
                .getUISettings()
                .getRecipeViewerSettings()
                .addExclusionArea(this);
        }
        this.getSettingPanel()
            .updateTabStates();
    }

    public IStorageWrapper getStorageWrapper() {
        return this.storageWrapper;
    }

    public StoragePanel getStoragePanel() {
        return this.storagePanel;
    }

    public StorageSettingPanel getSettingPanel() {
        return this.settingPanel;
    }

    public Tab getParentTab() {
        return this.parentTab;
    }
}
