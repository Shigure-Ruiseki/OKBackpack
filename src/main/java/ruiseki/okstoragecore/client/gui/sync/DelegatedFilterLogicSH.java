package ruiseki.okstoragecore.client.gui.sync;

import java.util.function.Supplier;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.upgrades.FilterLogic;

public class DelegatedFilterLogicSH extends SyncHandlerBase<DelegatedFilterLogicSH> {

    private final IStorageWrapper storageWrapper;
    private final StoragePanel storagePanel;
    private final int slotIndex;

    private Supplier<FilterLogic> supplier;

    public DelegatedFilterLogicSH(IStorageWrapper storageWrapper, StoragePanel storagePanel, int slotIndex) {
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
        this.slotIndex = slotIndex;

        this.supplier = () -> null;
    }

    public FilterLogic getFilterLogic() {
        return this.supplier.get();
    }

    public void setFilterLogic(Supplier<FilterLogic> supplier) {
        this.supplier = supplier;
    }

    public IStorageWrapper getStorageWrapper() {
        return this.storageWrapper;
    }

    public StoragePanel getStoragePanel() {
        return this.storagePanel;
    }

    public int getSlotIndex() {
        return this.slotIndex;
    }
}
