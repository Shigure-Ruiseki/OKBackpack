package ruiseki.okstoragecore.upgrades.crafting;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;

public class CraftingResultHandler implements IItemHandlerModifiable {

    private ItemStack result = ItemHelpers.EMPTY;

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot == 0 ? result : ItemHelpers.EMPTY;
    }

    public void setResult(ItemStack stack) {
        this.result = ItemHelpers.isEmpty(stack) ? ItemHelpers.EMPTY : stack.copy();
    }

    @Override
    public void setStackInSlot(int slot, @Nullable ItemStack stack) {

    }

    @Override
    public @Nullable ItemStack insertItem(int slot, @Nullable ItemStack stack, boolean simulate) {
        return null;
    }

    @Override

    public @Nullable ItemStack extractItem(int slot, int amount, boolean simulate) {
        return null;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }
}
