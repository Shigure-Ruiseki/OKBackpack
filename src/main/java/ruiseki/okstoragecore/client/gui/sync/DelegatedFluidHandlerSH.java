package ruiseki.okstoragecore.client.gui.sync;

import java.util.function.Supplier;

import ruiseki.okstoragecore.client.gui.container.StorageContainer;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedFluidHandler;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.upgrades.FluidTankHandler;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public class DelegatedFluidHandlerSH extends SyncHandlerBase<DelegatedFluidHandlerSH> {

    private final Supplier<StorageContainer> containerSupplier;
    private final int slotIndex;
    private final int wrappedTankAmount;

    private final DelegatedFluidHandler fluidHandler;

    public DelegatedFluidHandlerSH(StoragePanel panel, Supplier<StorageContainer> containerSupplier, int slotIndex,
        int wrappedTankAmount) {
        super(panel);
        this.containerSupplier = containerSupplier;
        this.slotIndex = slotIndex;
        this.wrappedTankAmount = wrappedTankAmount;

        this.fluidHandler = new DelegatedFluidHandler(
            () -> new FluidTankHandler(this.wrappedTankAmount),
            this.wrappedTankAmount);
    }

    public void setFluidHandler(Supplier<FluidTankHandler> supplier) {
        this.fluidHandler.setDelegated(supplier);
    }

    public DelegatedFluidHandler getFluidHandler() {
        return this.fluidHandler;
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

    public int getWrappedTankAmount() {
        return wrappedTankAmount;
    }
}
