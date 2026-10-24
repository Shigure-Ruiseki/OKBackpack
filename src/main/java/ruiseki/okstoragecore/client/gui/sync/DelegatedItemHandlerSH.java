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

    private final Supplier<StorageContainer> containerSupplier;
    private final int slotIndex;
    private final int wrappedSlotAmount;

    private final DelegatedItemHandler itemHandler;

    public DelegatedItemHandlerSH(IStorageWrapper storageWrapper, StoragePanel storagePanel,
        Supplier<StorageContainer> containerSupplier, int slotIndex, int wrappedSlotAmount) {
        super(storageWrapper, storagePanel);
        this.slotIndex = slotIndex;
        this.wrappedSlotAmount = wrappedSlotAmount;
        this.containerSupplier = containerSupplier;

        this.itemHandler = new DelegatedItemHandler(() -> EmptyHandler.INSTANCE, this.wrappedSlotAmount);
    }

    public void setItemHandler(Supplier<IItemHandlerModifiable> supplier) {
        this.itemHandler.setDelegated(supplier);
    }

    public IItemHandlerModifiable getItemHandler() {
        return this.itemHandler;
    }

    public IUpgradeWrapper getUpgradeWrapper() {
        return this.getWrapper()
            .getUpgradeHandler()
            .getSlotWrapper(this.getSlotIndex());
    }

    public Supplier<StorageContainer> getContainerSupplier() {
        return containerSupplier;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public int getWrappedSlotAmount() {
        return wrappedSlotAmount;
    }
}
