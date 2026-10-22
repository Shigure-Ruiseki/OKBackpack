package ruiseki.okstoragecore.client.gui.sync;

import java.util.function.Supplier;

import ruiseki.okbackpack.client.gui.handler.DelegatedItemHandler;
import ruiseki.okcore.item.handler.EmptyHandler;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.container.StorageContainer;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public class DelegatedItemHandlerSH extends SyncHandlerBase<DelegatedItemHandlerSH> {

    private final IStorageWrapper storageWrapper;
    private final StoragePanel storagePanel;
    private final Supplier<StorageContainer> containerSupplier;
    private final int slotIndex;
    private final int wrappedSlotAmount;

    private final DelegatedItemHandler supplier;

    public DelegatedItemHandlerSH(IStorageWrapper storageWrapper, StoragePanel storagePanel,
        Supplier<StorageContainer> containerSupplier, int slotIndex, int wrappedSlotAmount) {
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
        this.slotIndex = slotIndex;
        this.wrappedSlotAmount = wrappedSlotAmount;
        this.containerSupplier = containerSupplier;

        this.supplier = new DelegatedItemHandler(() -> EmptyHandler.INSTANCE, this.wrappedSlotAmount);
    }

    public void setItemHandler(Supplier<IItemHandlerModifiable> supplier) {
        this.supplier.setDelegated(supplier);
    }

    public IItemHandlerModifiable getItemHandler() {
        return this.supplier;
    }

    public IUpgradeWrapper getUpgradeWrapper() {
        return this.getStorageWrapper()
            .getUpgradeHandler()
            .getSlotWrapper(this.getSlotIndex());
    }

    public IStorageWrapper getStorageWrapper() {
        return storageWrapper;
    }

    public StoragePanel getStoragePanel() {
        return storagePanel;
    }

    public Supplier<StorageContainer> getContainerSupplier() {
        return containerSupplier;
    }

    public int getSlotIndex() {
        return slotIndex;
    }
}
