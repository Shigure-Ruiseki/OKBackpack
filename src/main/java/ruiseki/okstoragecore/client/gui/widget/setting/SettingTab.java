package ruiseki.okstoragecore.client.gui.widget.setting;

import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.ExpandedTab;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.StorageSettingPanel;
import ruiseki.okstoragecore.client.gui.widget.Tab;

public class SettingTab extends ExpandedTab {

    private final IStorageWrapper storageWrapper;
    private final StoragePanel storagePanel;
    private final StorageSettingPanel settingPanel;
    private final Tab parentTab;

    public SettingTab(IDrawable delegatedIcon, String titleKey, Tab.ExpandDirection expandDirection,
        IStorageWrapper storageWrapper, StoragePanel storagePanel, StorageSettingPanel settingPanel, Tab parentTab) {
        super(delegatedIcon, titleKey, expandDirection);
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
        this.settingPanel = settingPanel;
        this.parentTab = parentTab;

        this.getContentsColumn()
            .child(this.addSettingsColumn());
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

    protected Flow addSettingsColumn() {
        return Flow.column()
            .crossAxisAlignment(Alignment.CrossAxis.START)
            .childPadding(2)
            .coverChildren();
    }

    protected IStorageWrapper getStorageWrapper() {
        return this.storageWrapper;
    }

    protected StoragePanel getStoragePanel() {
        return this.storagePanel;
    }

    protected StorageSettingPanel getSettingPanel() {
        return this.settingPanel;
    }

    protected Tab getParentTab() {
        return this.parentTab;
    }
}
