package ruiseki.storagecore.client.gui.widget.update;

import java.util.HashMap;
import java.util.Map;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.StoragePanel;

public class UpgradeSlotUpdate {

    private final StoragePanel panel;
    private final IStorageWrapper wrapper;
    private final int slotIndex;
    private final PanelSyncManager syncManager;

    final Map<String, Object> components = new HashMap<>();

    public UpgradeSlotUpdate(IStorageWrapper wrapper, StoragePanel panel, int slotIndex) {
        this.wrapper = wrapper;
        this.panel = panel;
        this.slotIndex = slotIndex;
        this.syncManager = panel.getSyncManager();

        for (IUpgradeSlotUpdateFactory factory : UpgradeSlotUpdateRegistry.getFactories()) {
            factory.build(this);
        }
    }

    public <T> void put(String key, T value) {
        components.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {

        Object obj = this.components.get(key);

        if (obj == null) {
            return null;
        }

        return (T) obj;
    }

    public StoragePanel getPanel() {
        return this.panel;
    }

    public IStorageWrapper getWrapper() {
        return this.wrapper;
    }

    public int getSlotIndex() {
        return this.slotIndex;
    }

    public PanelSyncManager getSyncManager() {
        return this.syncManager;
    }
}
