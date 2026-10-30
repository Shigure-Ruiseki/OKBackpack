package ruiseki.okstoragecore.inventory;

import java.util.function.Supplier;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.core.mixins.early.minecraft.InventoryCraftingAccessor;
import com.cleanroommc.modularui.utils.Platform;

import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.handler.IItemHandler;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okstoragecore.client.gui.container.StorageContainer;

public class InventoryCraftingWrapper extends InventoryCrafting {

    public final IItemHandlerModifiable delegate;
    public final Supplier<StorageContainer> supplier;
    public final int size;
    public final int startIndex;

    public InventoryCraftingWrapper(Supplier<StorageContainer> supplier, int width, int height,
        IItemHandlerModifiable delegate, int startIndex) {
        super(supplier.get(), width, height);
        this.supplier = supplier;
        this.size = width * height;
        if (startIndex + this.size > delegate.getSlots()) {
            throw new IllegalArgumentException(
                "Inventory does not have enough slots for given size. Requires " + (startIndex + this.size)
                    + " slots, but only has "
                    + delegate.getSlots()
                    + " slots!");
        }
        this.delegate = delegate;
        this.startIndex = startIndex;
        for (int i = 0; i < this.size; i++) {
            ItemStack stack = this.delegate.getStackInSlot(i + this.startIndex);
            updateSnapshot(i, stack);
        }
    }

    private ItemStack[] getBackingList() {
        return ((InventoryCraftingAccessor) this).getStackList();
    }

    public Container getContainer() {
        if (eventHandler == null) eventHandler = this.supplier.get();
        return this.eventHandler;
    }

    private void updateSnapshot(int index, ItemStack stack) {
        getBackingList()[index] = Platform.copyStack(stack);
    }

    public void detectChanges() {
        // detect changes from snapshot and notify container
        boolean notify = false;
        for (int i = 0; i < this.size; i++) {
            ItemStack stack = getBackingList()[i];
            ItemStack current = this.delegate.getStackInSlot(i + this.startIndex);
            if (ItemHelpers.isEmpty(current) && current != ItemHelpers.EMPTY) {
                current = ItemHelpers.EMPTY;
                this.delegate.setStackInSlot(i + this.startIndex, ItemHelpers.EMPTY);
            }
            if (ItemHelpers.isEmpty(stack) != ItemHelpers.isEmpty(current)
                || (!ItemHelpers.isEmpty(stack) && !ItemHandlerHelpers.canItemStacksStack(stack, current))) {
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
        for (int i = 0; i < this.size; i++) {
            if (!ItemHelpers.isEmpty(getStackInSlot(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        int real = this.startIndex + index;
        return real >= 0 && real < this.delegate.getSlots() ? this.delegate.getStackInSlot(real) : ItemHelpers.EMPTY;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.setSlot(index, stack, true);
    }

    public void setSlot(int index, ItemStack stack, boolean notifyContainer) {
        this.delegate.setStackInSlot(this.startIndex + index, stack);
        if (notifyContainer) this.notifyContainer();
    }

    @Override
    public @NotNull ItemStack decrStackSize(int index, int count) {
        return this.decrStackSize(index, count, true);
    }

    public ItemStack decrStackSize(int index, int count, boolean notifyContainer) {
        if (index < 0 || index >= this.size || count <= 0) return ItemHelpers.EMPTY;

        int real = this.startIndex + index;
        ItemStack stack = this.delegate.getStackInSlot(real);

        if (ItemHelpers.isEmpty(stack)) return ItemHelpers.EMPTY;

        ItemStack result = stack.splitStack(count);

        if (ItemHelpers.isEmpty(stack)) {
            this.delegate.setStackInSlot(real, ItemHelpers.EMPTY);
        }

        if (notifyContainer) this.notifyContainer();

        return result;
    }

    public ItemStack removeStackFromSlot(int index) {
        return this.removeStackFromSlot(index, true);
    }

    public ItemStack removeStackFromSlot(int index, boolean notifyContainer) {
        if (index < 0 || index >= this.size) return ItemHelpers.EMPTY;

        int real = this.startIndex + index;
        ItemStack stack = this.delegate.getStackInSlot(real);

        this.delegate.setStackInSlot(real, ItemHelpers.EMPTY);

        if (notifyContainer) notifyContainer();

        return stack;
    }

    public void clear() {
        for (int i = 0; i < this.size; i++) {
            this.setSlot(i, ItemHelpers.EMPTY, false);
        }
    }

    public void notifyContainer() {
        this.getContainer()
            .onCraftMatrixChanged(this);
    }
}
