package ruiseki.okbackpack.common.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.common.block.BackpackWrapper;

public class BackpackWrapperInventoryAdapter implements IInventory {

    private final BackpackWrapper wrapper;

    public BackpackWrapperInventoryAdapter(BackpackWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public int getSizeInventory() {
        return wrapper.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return wrapper.getStackInSlot(slot);
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return wrapper.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = wrapper.getStackInSlot(slot);
        if (stack != null) {
            wrapper.setStackInSlot(slot, null);
        }
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        wrapper.setStackInSlot(slot, stack);
    }

    @Override
    public String getInventoryName() {
        return wrapper.getInventoryName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return wrapper.hasCustomInventoryName();
    }

    @Override
    public int getInventoryStackLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void markDirty() {
        wrapper.markDirty();
        wrapper.writeToItem();
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {
        wrapper.writeToItem();
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return wrapper.getStackHandler()
            .isItemValid(slot, stack);
    }
}
