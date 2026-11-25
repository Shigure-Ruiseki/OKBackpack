package ruiseki.okbackpack.common.helpers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IInventoryInteractionUpgrade;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okcore.helper.ItemHandlerHelpers;

public class InventoryInteractionHelpers {

    private InventoryInteractionHelpers() {}

    /**
     * Attempts deposit/restock interaction with the container at the target block.
     * On the client side, only checks eligibility (returns true if upgrades exist) to suppress ghost block placement.
     *
     * @return true if interaction upgrades exist for this target container
     */
    public static boolean tryInventoryInteraction(ItemStack backpackStack, EntityPlayer player, World world, int x,
        int y, int z, ForgeDirection side) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof IInventory inventory)) return false;

        BackpackWrapper wrapper = new BackpackWrapper(
            backpackStack,
            (BlockBackpack.ItemBackpack) backpackStack.getItem());
        Map<Integer, IInventoryInteractionUpgrade> upgrades = wrapper
            .gatherCapabilityUpgrades(IInventoryInteractionUpgrade.class);

        if (upgrades.isEmpty()) return false;

        // On the client we only need to signal "yes, this will be handled" so that
        // super.onItemUse (block placement) is suppressed.
        if (world.isRemote) return true;

        boolean anySuccess = false;
        for (IInventoryInteractionUpgrade upgrade : upgrades.values()) {
            if (upgrade.onInteract(inventory, player, side)) {
                anySuccess = true;
            }
        }

        if (anySuccess) {
            wrapper.writeToItem();
        }

        // Target is a container and upgrades exist — always consume the interaction
        // to prevent block placement regardless of whether any items were transferred.
        return true;
    }

    /**
     * Transfers items from backpack to target container.
     * Respects ISidedInventory and isItemValidForSlot restrictions.
     *
     * @param side the face of the target container that was clicked
     * @return number of stacks transferred
     */
    public static int transferToInventory(IStorageWrapper backpackWrapper, IInventory target,
        Predicate<ItemStack> filter, ForgeDirection side) {
        int transferred = 0;

        int[] accessibleSlots = getAccessibleSlots(target, side);

        for (int bpSlot = 0; bpSlot < backpackWrapper.getSlots(); bpSlot++) {
            ItemStack stack = backpackWrapper.getStackInSlot(bpSlot);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!filter.test(stack)) continue;

            int originalSize = stack.stackSize;
            ItemStack toInsert = ItemHandlerHelpers.copyStackWithSize(stack, stack.stackSize);

            int inserted = insertIntoInventory(target, toInsert, accessibleSlots, false);
            if (inserted > 0) {
                // Deduct transferred amount from backpack
                backpackWrapper.extractItem(bpSlot, inserted, false);
                transferred++;
            }
        }

        return transferred;
    }

    /**
     * Transfers items from source container to backpack.
     * Respects ISidedInventory canExtractItem restrictions and backpack's own filter logic.
     *
     * @param side the face of the source container that was clicked
     * @return number of stacks transferred
     */
    public static int transferFromInventory(IInventory source, IStorageWrapper backpackWrapper,
        Predicate<ItemStack> filter, ForgeDirection side) {
        int transferred = 0;

        int[] accessibleSlots = getAccessibleSlots(source, side);

        for (int slot : accessibleSlots) {
            ItemStack stack = source.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!filter.test(stack)) continue;

            // Check ISidedInventory extraction permission
            if (source instanceof ISidedInventory sided) {
                if (!sided.canExtractItem(slot, stack, side.ordinal())) continue;
            }

            // Try inserting into backpack
            ItemStack toInsert = ItemHandlerHelpers.copyStackWithSize(stack, stack.stackSize);
            ItemStack remaining = backpackWrapper.insertItem(toInsert, false);

            int inserted = stack.stackSize - (remaining != null ? remaining.stackSize : 0);
            if (inserted > 0) {
                // Deduct from source container
                stack.stackSize -= inserted;
                if (stack.stackSize <= 0) {
                    source.setInventorySlotContents(slot, null);
                } else {
                    source.setInventorySlotContents(slot, stack);
                }
                source.markDirty();
                transferred++;
            }
        }

        return transferred;
    }

    /**
     * Collects unique stacks from the inventory (for "filter by inventory contents" mode).
     */
    public static Set<StackKey> getUniqueStacks(IInventory inventory) {
        Set<StackKey> stacks = new HashSet<>();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null && stack.stackSize > 0) {
                stacks.add(new StackKey(stack));
            }
        }
        return stacks;
    }

    /**
     * Returns accessible slot indices. Uses ISidedInventory if available, otherwise all slots.
     */
    private static int[] getAccessibleSlots(IInventory inventory, ForgeDirection side) {
        if (inventory instanceof ISidedInventory sided) {
            return sided.getAccessibleSlotsFromSide(side.ordinal());
        }
        int[] slots = new int[inventory.getSizeInventory()];
        for (int i = 0; i < slots.length; i++) {
            slots[i] = i;
        }
        return slots;
    }

    /**
     * Inserts items into the target container, respecting isItemValidForSlot and ISidedInventory.
     *
     * @return number of items inserted
     */
    private static int insertIntoInventory(IInventory inventory, ItemStack stack, int[] accessibleSlots,
        boolean simulate) {
        if (stack == null || stack.stackSize <= 0) return 0;

        int totalInserted = 0;
        int remaining = stack.stackSize;

        // Pass 1: merge into existing matching stacks
        for (int slot : accessibleSlots) {
            if (remaining <= 0) break;

            ItemStack existing = inventory.getStackInSlot(slot);
            if (existing == null) continue;
            if (!ItemHandlerHelpers.canItemStacksStack(existing, stack)) continue;
            if (!inventory.isItemValidForSlot(slot, stack)) continue;

            if (inventory instanceof ISidedInventory sided) {
                if (!sided.canInsertItem(slot, stack, ForgeDirection.UNKNOWN.ordinal())) continue;
            }

            int slotLimit = Math.min(inventory.getInventoryStackLimit(), existing.getMaxStackSize());
            int space = slotLimit - existing.stackSize;
            if (space <= 0) continue;

            int toInsert = Math.min(remaining, space);
            if (!simulate) {
                existing.stackSize += toInsert;
                inventory.setInventorySlotContents(slot, existing);
            }
            remaining -= toInsert;
            totalInserted += toInsert;
        }

        // Pass 2: insert into empty slots
        for (int slot : accessibleSlots) {
            if (remaining <= 0) break;

            ItemStack existing = inventory.getStackInSlot(slot);
            if (existing != null) continue;
            if (!inventory.isItemValidForSlot(slot, stack)) continue;

            if (inventory instanceof ISidedInventory sided) {
                if (!sided.canInsertItem(slot, stack, ForgeDirection.UNKNOWN.ordinal())) continue;
            }

            int slotLimit = Math.min(inventory.getInventoryStackLimit(), stack.getMaxStackSize());
            int toInsert = Math.min(remaining, slotLimit);
            if (!simulate) {
                inventory.setInventorySlotContents(slot, ItemHandlerHelpers.copyStackWithSize(stack, toInsert));
            }
            remaining -= toInsert;
            totalInserted += toInsert;
        }

        if (totalInserted > 0 && !simulate) {
            inventory.markDirty();
        }

        return totalInserted;
    }

    /**
     * Wrapper for comparing ItemStacks in a Set, ignoring stack size.
     */
    public record StackKey(int itemId, int metadata) {

        public StackKey(ItemStack stack) {
            this(Item.getIdFromItem(stack.getItem()), stack.getItemDamage());
        }

        public boolean matches(ItemStack stack) {
            return stack != null && Item.getIdFromItem(stack.getItem()) == itemId && stack.getItemDamage() == metadata;
        }
    }
}
