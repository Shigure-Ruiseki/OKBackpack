package ruiseki.okstoragecore.client.gui.slot;

import ruiseki.okcore.client.mui.gui.component.slot.ModularItemSlot;
import ruiseki.okcore.item.handler.IItemHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public class IndexedModularSlot extends ModularItemSlot {

    protected final IStorageWrapper storageWrapper;
    protected final StoragePanel storagePanel;
    protected final int upgradeIndex;

    public IndexedModularSlot(IItemHandler itemHandler, int index, IStorageWrapper storageWrapper,
        StoragePanel storagePanel, int upgradeIndex) {
        super(itemHandler, index);
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
        this.upgradeIndex = upgradeIndex;
    }

    public IStorageWrapper getStorageWrapper() {
        return storageWrapper;
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
