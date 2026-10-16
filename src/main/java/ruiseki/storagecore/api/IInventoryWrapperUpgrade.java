package ruiseki.storagecore.api;

import ruiseki.storagecore.inventory.ITrackedContentsItemHandler;

public interface IInventoryWrapperUpgrade {

    ITrackedContentsItemHandler wrapInventory(ITrackedContentsItemHandler inventory);
}
