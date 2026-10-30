package ruiseki.okstoragecore.inventory;

import java.util.ArrayList;
import java.util.List;

import ruiseki.okstoragecore.api.IIOFilterUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.FilterLogic;

public class InventoryIOHandler {

    private final ITrackedContentsItemHandler filteredItemHandler;

    public InventoryIOHandler(IStorageWrapper storageWrapper) {
        List<FilterLogic> inputFilters = new ArrayList<>();
        List<FilterLogic> outputFilters = new ArrayList<>();

        addFilters(storageWrapper, inputFilters, outputFilters);

        ITrackedContentsItemHandler modifiedInventory = storageWrapper.getInventoryForUpgradeProcessing();
        if (inputFilters.isEmpty() && outputFilters.isEmpty()) {
            filteredItemHandler = modifiedInventory;
        } else {
            filteredItemHandler = new FilteredItemHandler.Modifiable(modifiedInventory, inputFilters, outputFilters);
        }
    }

    public ITrackedContentsItemHandler getFilteredItemHandler() {
        return filteredItemHandler;
    }

    private void addFilters(IStorageWrapper storageWrapper, List<FilterLogic> inputFilters,
        List<FilterLogic> outputFilters) {
        List<IIOFilterUpgrade> filterWrappers = storageWrapper.getUpgradeHandler()
            .getWrappersThatImplement(IIOFilterUpgrade.class);

        for (IIOFilterUpgrade wrapper : filterWrappers) {
            wrapper.getInputFilter()
                .ifPresent(inputFilters::add);
            wrapper.getOutputFilter()
                .ifPresent(outputFilters::add);
        }
    }
}
