package ruiseki.okstoragecore.client.gui.sync;

import java.util.function.Supplier;

import ruiseki.okstoragecore.client.gui.container.StorageContainer;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.upgrades.FluidFilterLogic;

public class DelegatedFluidFilterLogicSH extends DelegatedFluidHandlerSH {

    private Supplier<FluidFilterLogic> filterLogic;

    public DelegatedFluidFilterLogicSH(StoragePanel storagePanel, Supplier<StorageContainer> containerSupplier,
        int slotIndex, int wrappedTankAmount) {
        super(storagePanel, containerSupplier, slotIndex, wrappedTankAmount);
        this.filterLogic = () -> new FluidFilterLogic(null, stack -> {}, this.getWrappedTankAmount());
    }

    public FluidFilterLogic getFilterLogic() {
        return this.filterLogic.get();
    }

    public void setFilterLogic(Supplier<FluidFilterLogic> filterLogic) {
        this.filterLogic = filterLogic;
        this.setFluidHandler(() -> getFilterLogic().getFilterHandler());
    }
}
