package ruiseki.backpack.backpack.wrapper;

import java.util.List;

import ruiseki.okstoragecore.api.IInventoryWrapperUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.inventory.ITrackedContentsItemHandler;

public class InventoryModificationHandler {

    private final IStorageWrapper backpackWrapper;
    private ITrackedContentsItemHandler modifiedInventoryHandler;

    public InventoryModificationHandler(IStorageWrapper backpackWrapper) {
        this.backpackWrapper = backpackWrapper;
    }

    public ITrackedContentsItemHandler getModifiedInventoryHandler() {
        if (modifiedInventoryHandler == null) {
            initializeWrappedInventory(backpackWrapper.getInventoryHandler());
        }
        return modifiedInventoryHandler;
    }

    private void initializeWrappedInventory(ITrackedContentsItemHandler inventoryHandler) {
        List<IInventoryWrapperUpgrade> inventoryWrapperUpgrades = backpackWrapper.getUpgradeHandler()
            .getWrappersThatImplement(IInventoryWrapperUpgrade.class);

        ITrackedContentsItemHandler wrappedHandler = inventoryHandler;
        for (IInventoryWrapperUpgrade inventoryWrapperUpgrade : inventoryWrapperUpgrades) {
            wrappedHandler = inventoryWrapperUpgrade.wrapInventory(wrappedHandler);
        }

        modifiedInventoryHandler = wrappedHandler;
    }
}
