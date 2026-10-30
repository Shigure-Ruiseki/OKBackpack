package ruiseki.okstoragecore.client.gui.slot;

import ruiseki.okcore.client.mui.gui.component.slot.ModularItemSlot;
import ruiseki.okcore.item.handler.IItemHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public class IndexedModularSlot extends ModularItemSlot {

    protected final StoragePanel storagePanel;
    protected final int upgradeIndex;

    public IndexedModularSlot(IItemHandler itemHandler, int index, StoragePanel storagePanel, int upgradeIndex) {
        super(itemHandler, index);
        this.storagePanel = storagePanel;
        this.upgradeIndex = upgradeIndex;
    }

    public IStorageWrapper getStorageWrapper() {
        return this.getStoragePanel()
            .getWrapper();
    }

    public StoragePanel getStoragePanel() {
        return storagePanel;
    }

    public int getUpgradeIndex() {
        return upgradeIndex;
    }

    public IUpgradeWrapper getUpgradeWrapper() {
        return this.getStorageWrapper()
            .getUpgradeHandler()
            .getSlotWrapper(this.getUpgradeIndex());
    }
}
