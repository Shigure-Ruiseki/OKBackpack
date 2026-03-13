package ruiseki.okbackpack.client.gui.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;

import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okcore.helper.ItemStackHelpers;

public class BackpackItemStackHandler extends UpgradeItemStackHandler {

    private final BackpackWrapper wrapper;

    public final List<ItemStack> memorizedSlotStack;
    public final List<Boolean> memorizedSlotRespectNbtList;
    public final List<Boolean> sortLockedSlots;

    public BackpackItemStackHandler(int size, BackpackWrapper wrapper) {
        super(size);
        this.wrapper = wrapper;

        this.memorizedSlotStack = new ArrayList<>(size);
        this.memorizedSlotRespectNbtList = new ArrayList<>(size);
        this.sortLockedSlots = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            memorizedSlotStack.add(null);
            memorizedSlotRespectNbtList.add(false);
            sortLockedSlots.add(false);
        }
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (memorizedSlotStack.get(slot) == null) {
            return !(stack.getItem() instanceof BlockBackpack.ItemBackpack) || wrapper.canNestBackpack();
        }
        if (memorizedSlotRespectNbtList.get(slot)) {
            return ItemStack.areItemStacksEqual(stack, memorizedSlotStack.get(slot));
        }
        return ItemStackHelpers.areItemsEqualIgnoreDurability(stack, memorizedSlotStack.get(slot));
    }

    @Override
    public int getStackLimit(int slot, ItemStack stack) {
        return stack.getMaxStackSize() * wrapper.getTotalStackMultiplier();
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64 * wrapper.getTotalStackMultiplier();
    }

    public ItemStack prioritizedInsertion(int slotIndex, ItemStack stack, boolean simulate) {
        if (stack != null && !wrapper.canNestBackpack() && stack.getItem() instanceof BlockBackpack.ItemBackpack) {
            return stack;
        }

        stack = insertItemToMemorySlots(stack, simulate);
        return insertItem(slotIndex, stack, simulate);
    }

    public ItemStack insertItemToMemorySlots(ItemStack stack, boolean simulate) {
        if (stack == null) return null;
        for (int i = 0; i < memorizedSlotStack.size(); i++) {
            ItemStack mem = memorizedSlotStack.get(i);
            if (mem == null) continue;

            boolean match = memorizedSlotRespectNbtList.get(i) ? ItemStack.areItemStacksEqual(stack, mem)
                : stack.isItemEqual(mem);

            if (!match) continue;

            stack = insertItem(i, stack, simulate);
            if (stack == null) return null;
        }

        return stack;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack == null) {
            return null;
        }
        ItemStack existing = stacks.get(slot);

        int limit = getStackLimit(slot, stack);

        if (existing != null) {
            if (!ItemHandlerHelper.canItemStacksStack(stack, existing)) {
                return stack;
            }
            limit -= existing.stackSize;
        }

        if (limit <= 0) {
            return stack;
        }

        boolean reachedLimit = stack.stackSize > limit;

        if (!simulate) {
            if (existing == null) {
                stacks.set(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);
            } else {
                existing.stackSize += (reachedLimit ? limit : stack.stackSize);
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.stackSize - limit) : null;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return null;
        }
        ItemStack existing = getStackInSlot(slot);
        if (existing == null) {
            return null;
        }

        int slotMaxStackSize = existing.getMaxStackSize() * wrapper.getTotalStackMultiplier();
        int toExtract = Math.min(amount, slotMaxStackSize);

        if (existing.stackSize <= toExtract) {
            if (!simulate) {
                stacks.set(slot, null);
                onContentsChanged(slot);
            }
            return existing;
        } else {
            if (!simulate) {
                stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.stackSize - toExtract));
                onContentsChanged(slot);
            }
            return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
        }
    }

}
