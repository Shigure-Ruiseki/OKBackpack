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
            return !(stack.getItem() instanceof BlockBackpack.ItemBackpack) || wrapper.canAddStack(slot, stack);
        }
        if (memorizedSlotRespectNbtList.get(slot)) {
            return ItemStack.areItemStacksEqual(stack, memorizedSlotStack.get(slot));
        }
        return ItemStackHelpers.areItemsEqualIgnoreDurability(stack, memorizedSlotStack.get(slot));
    }

    @Override
    public int getStackLimit(int slot, ItemStack stack) {
        return (stack == null ? 64 : stack.getMaxStackSize()) * wrapper.applyStackLimitModifiers(1, slot, stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64 * wrapper.applySlotLimitModifiers(1, slot);
    }

    @Override
    public void resize(int newSize) {
        super.resize(newSize);
        syncListSize(this.memorizedSlotStack, newSize, null);
        syncListSize(this.memorizedSlotRespectNbtList, newSize, false);
        syncListSize(this.sortLockedSlots, newSize, false);
    }

    @Override
    public boolean isSizeInconsistent(int newSize) {
        return super.isSizeInconsistent(newSize) || this.memorizedSlotStack.size() != newSize
            || this.memorizedSlotRespectNbtList.size() != newSize
            || this.sortLockedSlots.size() != newSize;
    }

    public ItemStack prioritizedInsertion(int slot, ItemStack stack, boolean simulate) {
        if (stack == null || stack.stackSize <= 0) return stack;

        if (!wrapper.canAddStack(slot, stack)) {
            return stack;
        }

        stack = insertItemToMemorySlots(stack, simulate);
        return insertItem(slot, stack, simulate);
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
        if (amount == 0) return null;

        ItemStack existing = stacks.get(slot);
        if (existing == null) return null;

        int slotMaxStackSize = existing.getMaxStackSize() * wrapper.applyStackLimitModifiers(1, slot, existing);
        int toExtract = Math.min(amount, slotMaxStackSize);

        ItemStack extracted;

        if (existing.stackSize <= toExtract) {
            extracted = existing;
            if (!simulate) {
                stacks.set(slot, null);
                onContentsChanged(slot);
            }
        } else {
            extracted = ItemHandlerHelper.copyStackWithSize(existing, toExtract);
            if (!simulate) {
                stacks.set(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.stackSize - toExtract));
                onContentsChanged(slot);
            }
        }

        return extracted;
    }

}
