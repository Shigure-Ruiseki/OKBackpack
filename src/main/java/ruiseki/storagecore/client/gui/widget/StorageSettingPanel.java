package ruiseki.storagecore.client.gui.widget;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import ruiseki.storagecore.api.IStorageWrapper;

public class StorageSettingPanel extends ModularPanel {

    protected final IStorageWrapper wrapper;
    protected final StoragePanel storagePanel;

    protected final PanelSyncManager syncManager;
    protected final IPanelHandler panelHandler;

    public StorageSettingPanel(IStorageWrapper wrapper, StoragePanel storagePanel, PanelSyncManager syncManager,
        IPanelHandler panelHandler) {
        super("settings");
        this.wrapper = wrapper;
        this.storagePanel = storagePanel;
        this.syncManager = syncManager;
        this.panelHandler = panelHandler;

        this.size(
            6,
            this.getStoragePanel()
                .getArea().height)
            .relative(this.getStoragePanel())
            .top(0)
            .right(0);

    }

    public IStorageWrapper getWrapper() {
        return wrapper;
    }

    public StoragePanel getStoragePanel() {
        return storagePanel;
    }
}
