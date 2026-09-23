package ruiseki.okbackpack.mixins.late.tconstruct;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ruiseki.okbackpack.common.block.TEBackpack;
import tconstruct.tools.gui.ChestSlot;
import tconstruct.tools.inventory.CraftingStationContainer;

@Mixin(value = CraftingStationContainer.class, remap = false)
public abstract class MixinCraftingStationContainer extends Container {

    @Shadow
    protected abstract boolean moveToPlayerInventory(ItemStack stack);

    @Redirect(
        method = "slotClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/Container;slotClick(I I I Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack okbackpack$slotClick(Container container, int slotId, int clickedButton, int mode,
        EntityPlayer player) {
        if (!isBackpackSlot(slotId)) {
            return super.slotClick(slotId, clickedButton, mode, player);
        }

        if (mode == 0) {
            return handlePickup(slotId, clickedButton, player);
        }
        if (mode == 1) {
            return handleShiftClick(slotId, player);
        }
        if (mode == 2) {
            return handleHotbarSwap(slotId, clickedButton, player);
        }
        if (mode == 3) {
            return handleCreativePick(slotId, player);
        }
        if (mode == 4) {
            return handleDrop(slotId, clickedButton, player);
        }
        return super.slotClick(slotId, clickedButton, mode, player);
    }

    @Inject(method = "transferStackInSlot", at = @At("HEAD"), cancellable = true, remap = false)
    private void okbackpack$transferBackpackStack(EntityPlayer player, int index,
        CallbackInfoReturnable<ItemStack> cir) {
        if (index < 0 || index >= inventorySlots.size()) return;

        Slot slot = inventorySlots.get(index);
        if (!(slot instanceof ChestSlot) || !(slot.inventory instanceof TEBackpack)) return;

        ItemStack storedStack = slot.inventory.getStackInSlot(slot.getSlotIndex());
        if (storedStack == null || storedStack.stackSize <= 0) {
            cir.setReturnValue(null);
            return;
        }

        cir.setReturnValue(moveBackpackStackToPlayer(slot, player));
    }

    @Inject(method = "mergeItemStack", at = @At("HEAD"), cancellable = true, remap = false)
    private void okbackpack$mergeBackpackStacks(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex,
        CallbackInfoReturnable<Boolean> cir) {
        if (!containsBackpackSlot(startIndex, endIndex)) return;

        cir.setReturnValue(mergeBackpackStacks(stack, startIndex, endIndex, useEndIndex));
    }

    @Unique
    private ItemStack handlePickup(int slotId, int clickedButton, EntityPlayer player) {
        Slot slot = getContainerSlot(slotId);
        InventoryPlayer playerInventory = player.inventory;
        ItemStack stored = getBackpackStack(slot);
        ItemStack carried = playerInventory.getItemStack();
        ItemStack result = getStackCopy(stored);

        if (stored == null) {
            if (carried == null || !slot.isItemValid(carried)) return result;

            int amount = clickedButton == 0 ? carried.stackSize : 1;
            amount = Math.min(amount, getStorageLimit(slot));
            if (amount <= 0) return result;

            ItemStack placed = carried.splitStack(amount);
            setBackpackStack(slot, placed);
            if (carried.stackSize <= 0) playerInventory.setItemStack(null);
            syncSlot(slot);
            return result;
        }

        if (!slot.canTakeStack(player)) return result;

        if (carried == null) {
            int amount = clickedButton == 0 ? getTransferLimit(stored) : (stored.stackSize + 1) / 2;
            amount = Math.min(amount, getTransferLimit(stored));
            ItemStack extracted = extractBackpackStack(slot, amount);
            if (extracted != null && extracted.stackSize > 0) {
                playerInventory.setItemStack(extracted);
                slot.onPickupFromSlot(player, extracted);
                syncSlot(slot);
            }
            return result;
        }

        if (!canMerge(stored, carried) || !slot.isItemValid(carried)) return result;

        int available = getStorageLimit(slot) - stored.stackSize;
        if (available <= 0) {
            if (clickedButton == 0) {
                int amount = getTransferLimit(stored) - carried.stackSize;
                ItemStack extracted = extractBackpackStack(slot, amount);
                if (extracted != null && extracted.stackSize > 0) {
                    carried.stackSize += extracted.stackSize;
                    slot.onPickupFromSlot(player, extracted);
                    syncSlot(slot);
                }
            }
            return result;
        }

        int amount = clickedButton == 0 ? Math.min(carried.stackSize, available) : Math.min(1, available);
        if (amount <= 0) return result;

        ItemStack merged = stored.copy();
        merged.stackSize += amount;
        carried.stackSize -= amount;
        setBackpackStack(slot, merged);
        if (carried.stackSize <= 0) playerInventory.setItemStack(null);
        syncSlot(slot);
        return result;
    }

    @Unique
    private ItemStack handleShiftClick(int slotId, EntityPlayer player) {
        Slot slot = getContainerSlot(slotId);
        return moveBackpackStackToPlayer(slot, player);
    }

    @Unique
    private ItemStack handleHotbarSwap(int slotId, int hotbarIndex, EntityPlayer player) {
        if (hotbarIndex < 0 || hotbarIndex >= 9) return null;

        Slot slot = getContainerSlot(slotId);
        InventoryPlayer playerInventory = player.inventory;
        ItemStack stored = getBackpackStack(slot);
        ItemStack hotbar = playerInventory.getStackInSlot(hotbarIndex);
        ItemStack result = getStackCopy(stored);

        if (stored == null) {
            if (hotbar == null || !slot.isItemValid(hotbar)) return result;

            int amount = Math.min(hotbar.stackSize, getStorageLimit(slot));
            if (amount <= 0) return result;

            ItemStack placed = hotbar.copy();
            placed.stackSize = amount;
            hotbar.stackSize -= amount;
            setBackpackStack(slot, placed);
            playerInventory.setInventorySlotContents(hotbarIndex, hotbar.stackSize > 0 ? hotbar : null);
            syncSlot(slot);
            return result;
        }

        if (!slot.canTakeStack(player)) return result;

        if (hotbar == null) {
            ItemStack extracted = extractBackpackStack(slot, getTransferLimit(stored));
            if (extracted != null) {
                playerInventory.setInventorySlotContents(hotbarIndex, extracted);
                syncSlot(slot);
            }
            return result;
        }

        if (!canMerge(stored, hotbar)) return result;

        int available = hotbar.getMaxStackSize() - hotbar.stackSize;
        int amount = Math.min(stored.stackSize, Math.max(0, available));
        if (amount <= 0) return result;

        ItemStack extracted = extractBackpackStack(slot, amount);
        if (extracted != null) {
            hotbar.stackSize += extracted.stackSize;
            playerInventory.setInventorySlotContents(hotbarIndex, hotbar);
            syncSlot(slot);
        }
        return result;
    }

    @Unique
    private ItemStack handleCreativePick(int slotId, EntityPlayer player) {
        InventoryPlayer playerInventory = player.inventory;
        ItemStack stored = getBackpackStack(getContainerSlot(slotId));
        if (stored == null || playerInventory.getItemStack() != null || !player.capabilities.isCreativeMode) {
            return getStackCopy(stored);
        }

        ItemStack picked = stored.copy();
        picked.stackSize = getTransferLimit(picked);
        playerInventory.setItemStack(picked);
        return stored.copy();
    }

    @Unique
    private ItemStack handleDrop(int slotId, int clickedButton, EntityPlayer player) {
        Slot slot = getContainerSlot(slotId);
        ItemStack stored = getBackpackStack(slot);
        ItemStack result = getStackCopy(stored);
        if (stored == null || player.inventory.getItemStack() != null || !slot.canTakeStack(player)) return result;

        int amount = clickedButton == 0 ? getTransferLimit(stored) : 1;
        ItemStack dropped = extractBackpackStack(slot, amount);
        if (dropped != null) {
            player.dropPlayerItemWithRandomChoice(dropped, true);
            slot.onPickupFromSlot(player, dropped);
            syncSlot(slot);
        }
        return result;
    }

    @Unique
    private ItemStack moveBackpackStackToPlayer(Slot slot, EntityPlayer player) {
        if (!slot.canTakeStack(player)) return null;

        ItemStack storedStack = getBackpackStack(slot);
        if (storedStack == null || storedStack.stackSize <= 0) return null;

        ItemStack remainingStack = storedStack.copy();
        ItemStack result = remainingStack.copy();
        if (moveToPlayerInventory(remainingStack)) return null;

        int movedAmount = result.stackSize - remainingStack.stackSize;
        if (movedAmount <= 0) return null;

        slot.decrStackSize(movedAmount);
        slot.onSlotChanged();
        slot.onSlotChange(remainingStack, result);
        detectAndSendChanges();
        return result;
    }

    @Unique
    private boolean mergeBackpackStacks(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        boolean changed = false;
        int index = useEndIndex ? endIndex - 1 : startIndex;

        if (stack.isStackable()) {
            while (stack.stackSize > 0 && isInRange(index, startIndex, endIndex, useEndIndex)) {
                Slot slot = inventorySlots.get(index);
                ItemStack existing = getMergeStack(slot);
                if (existing != null && canMerge(stack, existing) && func_94530_a(stack, slot)) {
                    int limit = getMergeLimit(slot, stack, true);
                    int amount = Math.min(stack.stackSize, Math.max(0, limit - existing.stackSize));
                    if (amount > 0) {
                        existing.stackSize += amount;
                        stack.stackSize -= amount;
                        slot.onSlotChanged();
                        changed = true;
                    }
                }
                index += useEndIndex ? -1 : 1;
            }
        }

        index = useEndIndex ? endIndex - 1 : startIndex;
        while (stack.stackSize > 0 && isInRange(index, startIndex, endIndex, useEndIndex)) {
            Slot slot = inventorySlots.get(index);
            if (getMergeStack(slot) == null && slot.isItemValid(stack) && func_94530_a(stack, slot)) {
                int amount = Math.min(stack.stackSize, getMergeLimit(slot, stack, false));
                if (amount > 0) {
                    ItemStack placed = stack.copy();
                    placed.stackSize = amount;
                    slot.putStack(placed);
                    stack.stackSize -= amount;
                    changed = true;
                }
            }
            index += useEndIndex ? -1 : 1;
        }
        return changed;
    }

    @Unique
    private boolean containsBackpackSlot(int startIndex, int endIndex) {
        int first = Math.max(0, startIndex);
        int last = Math.min(inventorySlots.size(), endIndex);
        for (int index = first; index < last; index++) {
            if (isBackpackSlot(inventorySlots.get(index))) return true;
        }
        return false;
    }

    @Unique
    private static ItemStack getMergeStack(Slot slot) {
        ItemStack stack;
        if (slot instanceof ChestSlot && slot.inventory instanceof TEBackpack) {
            stack = slot.inventory.getStackInSlot(slot.getSlotIndex());
        } else {
            stack = slot.getStack();
        }
        return stack == null || stack.stackSize <= 0 ? null : stack;
    }

    @Unique
    private static int getMergeLimit(Slot slot, ItemStack stack, boolean respectItemLimit) {
        if (slot instanceof ChestSlot && slot.inventory instanceof TEBackpack) {
            return Math.max(0, slot.inventory.getInventoryStackLimit());
        }
        return respectItemLimit ? Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize())
            : slot.getSlotStackLimit();
    }

    @Unique
    private static boolean isInRange(int index, int startIndex, int endIndex, boolean useEndIndex) {
        return useEndIndex ? index >= startIndex : index < endIndex;
    }

    @Unique
    private void syncSlot(Slot slot) {
        slot.onSlotChanged();
        detectAndSendChanges();
    }

    @Unique
    private Slot getContainerSlot(int slotId) {
        return inventorySlots.get(slotId);
    }

    @Unique
    private boolean isBackpackSlot(int slotId) {
        if (slotId < 0 || slotId >= inventorySlots.size()) return false;
        return isBackpackSlot(getContainerSlot(slotId));
    }

    @Unique
    private static boolean isBackpackSlot(Slot slot) {
        return slot instanceof ChestSlot && slot.inventory instanceof TEBackpack;
    }

    @Unique
    private static ItemStack getBackpackStack(Slot slot) {
        return slot.inventory.getStackInSlot(slot.getSlotIndex());
    }

    @Unique
    private static void setBackpackStack(Slot slot, ItemStack stack) {
        slot.inventory.setInventorySlotContents(slot.getSlotIndex(), stack);
    }

    @Unique
    private static ItemStack extractBackpackStack(Slot slot, int amount) {
        if (amount <= 0) return null;
        return slot.inventory.decrStackSize(slot.getSlotIndex(), amount);
    }

    @Unique
    private static ItemStack getStackCopy(ItemStack stack) {
        return stack == null ? null : stack.copy();
    }

    @Unique
    private static boolean canMerge(ItemStack first, ItemStack second) {
        return first != null && second != null
            && first.getItem() == second.getItem()
            && (!first.getHasSubtypes() || first.getItemDamage() == second.getItemDamage())
            && ItemStack.areItemStackTagsEqual(first, second);
    }

    @Unique
    private static int getStorageLimit(Slot slot) {
        return Math.max(0, slot.inventory.getInventoryStackLimit());
    }

    @Unique
    private static int getTransferLimit(ItemStack stack) {
        return Math.min(127, stack.getMaxStackSize());
    }
}
