package ruiseki.okstoragecore.inventory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.inventory.ItemStackKey;
import ruiseki.okcore.item.handler.IItemHandler;
import ruiseki.okstoragecore.upgrades.FilterLogic;

public class FilteredItemHandler<T extends IItemHandler> implements IItemHandler {

    protected final T inventoryHandler;
    protected final List<FilterLogic> inputFilters;
    private final List<FilterLogic> outputFilters;

    public FilteredItemHandler(T inventoryHandler, List<FilterLogic> inputFilters, List<FilterLogic> outputFilters) {
        this.inventoryHandler = inventoryHandler;
        this.inputFilters = inputFilters;
        this.outputFilters = outputFilters;
    }

    @Override
    public int getSlots() {
        return inventoryHandler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventoryHandler.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (inputFilters.isEmpty()) {
            return inventoryHandler.insertItem(slot, stack, simulate);
        }

        for (FilterLogic filter : inputFilters) {
            if (filter.matchesFilter(stack)) {
                return inventoryHandler.insertItem(slot, stack, simulate);
            }
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (outputFilters.isEmpty()) {
            return inventoryHandler.extractItem(slot, amount, simulate);
        }

        for (FilterLogic filter : outputFilters) {
            if (filter.matchesFilter(getStackInSlot(slot))) {
                return inventoryHandler.extractItem(slot, amount, simulate);
            }
        }
        return ItemHelpers.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return inventoryHandler.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return inventoryHandler.isItemValid(slot, stack);
    }

    public static class Modifiable extends FilteredItemHandler<ITrackedContentsItemHandler>
        implements ITrackedContentsItemHandler {

        public Modifiable(ITrackedContentsItemHandler inventoryHandler, List<FilterLogic> inputFilters,
            List<FilterLogic> outputFilters) {
            super(inventoryHandler, inputFilters, outputFilters);
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            inventoryHandler.setStackInSlot(slot, stack);
        }

        @Override
        public ItemStack insertItem(ItemStack stack, boolean simulate) {
            if (inputFilters.isEmpty()) {
                return inventoryHandler.insertItem(stack, simulate);
            }

            for (FilterLogic filter : inputFilters) {
                if (filter.matchesFilter(stack)) {
                    return inventoryHandler.insertItem(stack, simulate);
                }
            }
            return stack;
        }

        @Override
        public Set<ItemStackKey> getTrackedStacks() {
            Set<ItemStackKey> ret = new HashSet<>();

            inventoryHandler.getTrackedStacks()
                .forEach(ts -> {
                    if (inputFiltersMatchStack(ts.getStack())) {
                        ret.add(ts);
                    }
                });

            return ret;
        }

        private boolean inputFiltersMatchStack(ItemStack stack) {
            for (FilterLogic filter : inputFilters) {
                if (filter.matchesFilter(stack)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void registerTrackingListeners(Consumer<ItemStackKey> onAddStackKey,
            Consumer<ItemStackKey> onRemoveStackKey, Runnable onAddFirstEmptySlot, Runnable onRemoveLastEmptySlot) {
            inventoryHandler.registerTrackingListeners(isk -> {
                if (inputFiltersMatchStack(isk.getStack())) {
                    onAddStackKey.accept(isk);
                }
            }, isk -> {
                if (inputFiltersMatchStack(isk.getStack())) {
                    onRemoveStackKey.accept(isk);
                }
            }, onAddFirstEmptySlot, onRemoveLastEmptySlot);
        }

        @Override
        public void unregisterStackKeyListeners() {
            inventoryHandler.unregisterStackKeyListeners();
        }

        @Override
        public boolean hasEmptySlots() {
            return inventoryHandler.hasEmptySlots();
        }

        @Override
        public int getInternalSlotLimit(int slot) {
            return inventoryHandler.getInternalSlotLimit(slot);
        }
    }
}
