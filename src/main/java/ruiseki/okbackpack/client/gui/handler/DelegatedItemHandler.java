package ruiseki.okbackpack.client.gui.handler;

import java.util.function.Supplier;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.item.handler.EmptyHandler;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;

public class DelegatedItemHandler implements IItemHandlerModifiable {

    private Supplier<IItemHandlerModifiable> delegated;
    private final int wrappedSlotAmount;

    public DelegatedItemHandler(Supplier<IItemHandlerModifiable> delegated, int wrappedSlotAmount) {
        this.delegated = delegated;
        this.wrappedSlotAmount = wrappedSlotAmount;
    }

    public void setDelegated(Supplier<IItemHandlerModifiable> delegated) {
        this.delegated = delegated;
    }

    public IItemHandlerModifiable get() {
        return this.delegated.get();
    }

    @Override
    public int getSlots() {
        IItemHandlerModifiable handler = this.get();

        if (handler != EmptyHandler.INSTANCE) {
            if (handler.getSlots() != wrappedSlotAmount) {
                throw new IllegalStateException(
                    "Mismatched delegated item handler slot amount: assumed to have " + wrappedSlotAmount
                        + " but actually got "
                        + handler.getSlots());
            }
        }

        return wrappedSlotAmount;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.get()
            .getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return this.get()
            .insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.get()
            .extractItem(slot, amount, simulate);
    }

    @Override
    public boolean isItemValid(int slot, @Nullable ItemStack stack) {
        return this.get()
            .isItemValid(slot, stack);
    }

    @Override
    public boolean isSlotFromInventory(int index, IInventory inventory, int invIndex) {
        return this.get()
            .isSlotFromInventory(index, inventory, invIndex);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.get()
            .getSlotLimit(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        this.get()
            .setStackInSlot(slot, stack);
    }
}
