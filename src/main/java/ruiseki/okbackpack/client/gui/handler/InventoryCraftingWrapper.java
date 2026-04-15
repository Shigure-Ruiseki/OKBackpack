package ruiseki.okbackpack.client.gui.handler;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.core.mixins.early.minecraft.InventoryCraftingAccessor;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;

/**
 * A crafting inventory which wraps a {@link IItemHandlerModifiable}. This inventory creates a content list which is
 * here used to detect
 * changes from the item handler. This is required as interacting with a slot will update the content, but will not
 * notify the container
 * to check for new recipes.
 */
public class InventoryCraftingWrapper extends InventoryCrafting {

    public final IItemHandlerModifiable delegate;
    public final int size;
    public final int startIndex;

    public InventoryCraftingWrapper(Container eventHandlerIn, int width, int height, IItemHandlerModifiable delegate,
        int startIndex) {
        super(eventHandlerIn, width, height);
        this.size = width * height + 1;
        if (startIndex + this.size > delegate.getSlots()) {
            throw new IllegalArgumentException(
                "Inventory does not have enough slots for given size. Requires " + (startIndex + this.size)
                    + " slots, but only has "
                    + delegate.getSlots()
                    + " slots!");
        }
        this.delegate = delegate;
        this.startIndex = startIndex;
        for (int i = 0; i < this.size - 1; i++) {
            ItemStack stack = this.delegate.getStackInSlot(i + this.startIndex);
            updateSnapshot(i, stack);
        }
    }

    private ItemStack[] getBackingList() {
        return ((InventoryCraftingAccessor) this).getStackList();
    }

    public Container getContainer() {
        return ((InventoryCraftingAccessor) this).getEventHandler();
    }

    private void updateSnapshot(int index, ItemStack stack) {
        getBackingList()[index] = Platform.copyStack(stack);
    }

    public void detectChanges() {
        // detect changes from snapshot and notify container
        boolean notify = false;
        for (int i = 0; i < this.size - 1; i++) {
            ItemStack stack = getBackingList()[i];
            ItemStack current = this.delegate.getStackInSlot(i + this.startIndex);
            if (Platform.isStackEmpty(current) && current != Platform.EMPTY_STACK) {
                current = Platform.EMPTY_STACK;
                this.delegate.setStackInSlot(i + this.startIndex, Platform.EMPTY_STACK);
            }
            if (Platform.isStackEmpty(stack) != Platform.isStackEmpty(current)
                || (!Platform.isStackEmpty(stack) && !ItemHandlerHelper.canItemStacksStack(stack, current))) {
                updateSnapshot(i, current);
                notify = true;
            }
        }
        if (notify) notifyContainer();
    }

    public IItemHandler getDelegate() {
        return delegate;
    }

    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getSizeInventory() {
        return size;
    }

    public boolean isEmpty() {
        for (int i = 0; i < this.size - 1; i++) {
            if (!Platform.isStackEmpty(getStackInSlot(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        // hide result slot from CraftingManager
        if (index >= this.size - 1) {
            return Platform.EMPTY_STACK;
        }

        int real = this.startIndex + index;
        return real >= 0 && real < this.delegate.getSlots() ? this.delegate.getStackInSlot(real) : Platform.EMPTY_STACK;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        setSlot(index, stack, true);
    }

    public void setSlot(int index, ItemStack stack, boolean notifyContainer) {
        this.delegate.setStackInSlot(this.startIndex + index, stack);
        if (notifyContainer) notifyContainer();
    }

    @Override
    public @NotNull ItemStack decrStackSize(int index, int count) {
        return decrStackSize(index, count, true);
    }

    public ItemStack decrStackSize(int index, int count, boolean notifyContainer) {
        if (index < 0 || index >= this.size - 1 || count <= 0) return Platform.EMPTY_STACK;

        int real = this.startIndex + index;
        ItemStack stack = this.delegate.getStackInSlot(real);

        if (Platform.isStackEmpty(stack)) return Platform.EMPTY_STACK;

        ItemStack result = stack.splitStack(count);

        if (Platform.isStackEmpty(stack)) {
            this.delegate.setStackInSlot(real, Platform.EMPTY_STACK);
        }

        if (notifyContainer) notifyContainer();

        return result;
    }

    public ItemStack removeStackFromSlot(int index) {
        return removeStackFromSlot(index, true);
    }

    public ItemStack removeStackFromSlot(int index, boolean notifyContainer) {
        if (index < 0 || index >= this.size - 1) return Platform.EMPTY_STACK;

        int real = this.startIndex + index;
        ItemStack stack = this.delegate.getStackInSlot(real);

        this.delegate.setStackInSlot(real, Platform.EMPTY_STACK);

        if (notifyContainer) notifyContainer();

        return stack;
    }

    public void clear() {
        for (int i = 0; i < this.size; i++) {
            setSlot(i, Platform.EMPTY_STACK, false);
        }
    }

    public void notifyContainer() {
        getContainer().onCraftMatrixChanged(this);
    }
}
