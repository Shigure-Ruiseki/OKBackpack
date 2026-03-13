package ruiseki.okbackpack.client.gui.handler;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;

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

    private IItemHandler getDelegated() {
        return delegated.get();
    }

    @Override
    public int getSlots() {
        IItemHandler handler = getDelegated();

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
        return getDelegated().getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return getDelegated().insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return getDelegated().extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return getDelegated().getSlotLimit(slot);
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        IItemHandler handler = getDelegated();

        if (handler instanceof IItemHandlerModifiable) {
            ((IItemHandlerModifiable) handler).setStackInSlot(slot, stack);
        }
    }
}
