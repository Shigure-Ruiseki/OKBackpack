package ruiseki.okstoragecore.inventory;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.handler.ItemStackHandler;

public class FilterItemStackHandler extends ItemStackHandler {

    private boolean onlyEmptyFilters = true;

    public FilterItemStackHandler(int size) {
        super(size);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemHelpers.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return ItemHelpers.EMPTY;
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);

        updateEmptyFilters();
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        updateEmptyFilters();
    }

    private void updateEmptyFilters() {
        onlyEmptyFilters = InventoryHelpers
            .iterate(this, (s, filter) -> ItemHelpers.isEmpty(filter), () -> true, result -> !result);
    }

    public boolean hasOnlyEmptyFilters() {
        return onlyEmptyFilters;
    }
}
