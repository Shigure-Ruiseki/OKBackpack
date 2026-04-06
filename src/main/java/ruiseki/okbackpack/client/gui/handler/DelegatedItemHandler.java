package ruiseki.okbackpack.client.gui.handler;

import java.util.function.Supplier;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.utils.item.EmptyHandler;
import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;

public class DelegatedItemHandler implements IItemHandlerModifiable {

    private Supplier<IItemHandler> delegated;
    private final int wrappedSlotAmount;

    public DelegatedItemHandler(Supplier<IItemHandler> delegated, int wrappedSlotAmount) {
        this.delegated = delegated;
        this.wrappedSlotAmount = wrappedSlotAmount;
    }

    public void setDelegated(Supplier<IItemHandler> delegated) {
        this.delegated = delegated;
    }

    public IItemHandler get() {
        return delegated.get();
    }

    @Override
    public int getSlots() {
        IItemHandler handler = get();

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
        return get().getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return get().insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return get().extractItem(slot, amount, simulate);
    }

    @Override
    public boolean isItemValid(int slot, @Nullable ItemStack stack) {
        return get().isItemValid(slot, stack);
    }

    @Override
    public boolean isSlotFromInventory(int index, IInventory inventory, int invIndex) {
        return get().isSlotFromInventory(index, inventory, invIndex);
    }

    @Override
    public int getSlotLimit(int slot) {
        return get().getSlotLimit(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        IItemHandler handler = get();

        if (handler instanceof IItemHandlerModifiable) {
            ((IItemHandlerModifiable) handler).setStackInSlot(slot, stack);
        }
    }
}
