package ruiseki.okstoragecore.client.gui.sync;

import java.util.function.Supplier;

import ruiseki.okstoragecore.client.gui.container.StorageContainer;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.upgrades.FilterLogic;

public class DelegatedFilterLogicSH extends DelegatedItemHandlerSH {

    private Supplier<FilterLogic> filterLogic;

    public DelegatedFilterLogicSH(StoragePanel storagePanel, Supplier<StorageContainer> containerSupplier,
        int slotIndex, int wrappedSlotAmount) {
        super(storagePanel, containerSupplier, slotIndex, wrappedSlotAmount);
        this.filterLogic = () -> new FilterLogic(null, stack -> {}, this.getWrappedSlotAmount());
    }

    public FilterLogic getFilterLogic() {
        return this.filterLogic.get();
    }

    public void setFilterLogic(Supplier<FilterLogic> filterLogic) {
        this.filterLogic = filterLogic;
        this.setItemHandler(
            () -> this.getFilterLogic()
                .getFilterHandler());
    }
}
