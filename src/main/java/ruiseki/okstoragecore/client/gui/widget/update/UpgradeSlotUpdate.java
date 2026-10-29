package ruiseki.okstoragecore.client.gui.widget.update;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;

import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.container.StorageContainer;
import ruiseki.okstoragecore.client.gui.container.UpgradeContainer;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class UpgradeSlotUpdate {

    private final StoragePanel panel;
    private final int slotIndex;
    private final PanelSyncManager syncManager;

    private UpgradeContainer upgradeContainer;

    final Map<String, Object> components = new HashMap<>();

    public UpgradeSlotUpdate(StoragePanel panel, int slotIndex) {
        this.panel = panel;
        this.slotIndex = slotIndex;
        this.syncManager = panel.getSyncManager();
        this.upgradeContainer = new UpgradeContainer(
            this.getWrapper(),
            this.getPanel(),
            this.getPlayer(),
            this.getSlotIndex());

        for (IUpgradeSlotUpdateFactory factory : UpgradeSlotUpdateRegistry.getFactories()) {
            factory.build(this);
        }
    }

    public void setUpgradeContainer(UpgradeContainer container) {
        this.upgradeContainer = container;
        this.upgradeContainer.onAdded(this);
    }

    public UpgradeContainer getUpgradeContainer() {
        return upgradeContainer;
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
        return this.getPanel()
            .getWrapper();
    }

    public EntityPlayer getPlayer() {
        return this.getPanel()
            .getPlayer();
    }

    public StorageContainer getContainer() {
        return this.getPanel()
            .getContainer();
    }

    public int getSlotIndex() {
        return this.slotIndex;
    }

    public PanelSyncManager getSyncManager() {
        return this.syncManager;
    }
}
