package ruiseki.okbackpack.client.gui.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.utils.item.ItemStackHandler;

public class BaseItemStackHandler extends ItemStackHandler {

    private Integer visualSize;

    public BaseItemStackHandler(int size) {
        super(size);
    }

    public void resize(int newSize) {
        List<ItemStack> newStacks = new ArrayList<>(newSize);

        for (int i = 0; i < newSize; i++) {
            if (i < stacks.size()) {
                newStacks.add(stacks.get(i));
            } else {
                newStacks.add(null);
            }
        }

        this.stacks = newStacks;

        if (visualSize != null && visualSize > newSize) {
            setVisualSize(newSize);
        }
    }

    public int getVisualSize() {
        return visualSize != null ? visualSize : getSlots();
    }

    public void setVisualSize(int visualSize) {
        this.visualSize = Math.max(0, Math.min(visualSize, getSlots()));
    }

    public boolean isSizeInconsistent(int newSize) {
        return newSize != stacks.size();
    }

    public static <T> void syncListSize(List<T> list, int newSize, T defaultValue) {
        int currentSize = list.size();
        if (newSize < currentSize) {
            list.subList(newSize, currentSize)
                .clear();
        } else {
            for (int i = currentSize; i < newSize; i++) {
                list.add(defaultValue);
            }
        }
    }

    /**
     * Returns the indices of non-null slots in the given range [from, to).
     */
    public int[] getFilledSlotsInRange(int from, int to) {
        List<Integer> indices = new ArrayList<>();
        for (int i = from; i < to && i < stacks.size(); i++) {
            if (stacks.get(i) != null) indices.add(i);
        }
        return indices.stream()
            .mapToInt(Integer::intValue)
            .toArray();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return isVisualSlot(slot) ? super.getStackInSlot(slot) : null;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (isVisualSlot(slot)) {
            super.setStackInSlot(slot, stack);
        }
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return isVisualSlot(slot) ? super.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return isVisualSlot(slot) ? super.extractItem(slot, amount, simulate) : null;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return super.isItemValid(slot, stack) && isVisualSlot(slot);
    }

    public boolean isVisualSlot(int slot) {
        if (slot < 0) {
            return false;
        }

        int maxVisualSlot = getVisualSize();
        if (slot >= maxVisualSlot) {
            return false;
        }

        int totalSlots = getSlots();
        if (slot >= totalSlots) {
            return false;
        }

        return true;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setInteger("VisualSize", getVisualSize());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        if (nbt.hasKey("VisualSize")) {
            setVisualSize(nbt.getInteger("VisualSize"));
        } else {
            setVisualSize(getSlots());
        }
    }
}
