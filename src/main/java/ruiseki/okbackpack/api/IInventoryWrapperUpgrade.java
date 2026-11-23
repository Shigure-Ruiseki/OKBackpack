package ruiseki.okbackpack.api;

import ruiseki.okstoragecore.inventory.ITrackedContentsItemHandler;

public interface IInventoryWrapperUpgrade {

    ITrackedContentsItemHandler wrapInventory(ITrackedContentsItemHandler inventory);
}
