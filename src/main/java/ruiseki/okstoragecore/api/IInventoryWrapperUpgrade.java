package ruiseki.okstoragecore.api;

import ruiseki.okstoragecore.inventory.ITrackedContentsItemHandler;

public interface IInventoryWrapperUpgrade {

    ITrackedContentsItemHandler wrapInventory(ITrackedContentsItemHandler inventory);
}
