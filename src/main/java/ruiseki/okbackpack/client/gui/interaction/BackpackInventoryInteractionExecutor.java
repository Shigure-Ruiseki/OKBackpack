package ruiseki.okbackpack.client.gui.interaction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;
import ruiseki.okbackpack.config.ModConfig;

public final class BackpackInventoryInteractionExecutor {

    private static final int PICKUP_MODE = 0;
    private static final int RIGHT_MOUSE_BUTTON = 1;

    private BackpackInventoryInteractionExecutor() {}

    public static boolean isSupportedClick(int slotId, int mouseButton, int mode) {
        if (!ModConfig.enableBackpackInventoryInteraction) {
            return false;
        }
        return slotId >= 0 && mouseButton == RIGHT_MOUSE_BUTTON && mode == PICKUP_MODE;
    }

    public static boolean tryExecute(Container container, EntityPlayer player, int slotId, int mouseButton, int mode) {
        if (!ModConfig.enableBackpackInventoryInteraction) {
            return false;
        }

        if (container == null || player == null
            || !isSupportedClick(slotId, mouseButton, mode)
            || slotId >= container.inventorySlots.size()) {
            return false;
        }

        Slot slot = (Slot) container.inventorySlots.get(slotId);
        ItemStack cursorStack = player.inventory.getItemStack();
        BackpackInventoryInteractionResult result = BackpackInventoryInteractionAnalyzer
            .analyze(player, slot, cursorStack);
        if (!result.canExecuteTransfer()) {
            return false;
        }

        switch (result.getKind()) {
            case INSERT_SLOT_INTO_CARRIED_BACKPACK:
                return executeSlotIntoCarriedBackpack(container, player, slot, cursorStack, result);
            case INSERT_CURSOR_INTO_SLOTTED_BACKPACK:
                return executeCursorIntoSlottedBackpack(container, player, slot, cursorStack);
            default:
                return false;
        }
    }

    private static boolean executeSlotIntoCarriedBackpack(Container container, EntityPlayer player, Slot slot,
        ItemStack backpackStack, BackpackInventoryInteractionResult result) {
        if (backpackStack == null || !BackpackEntityHelpers.isBackpackStack(backpackStack, false)) {
            return false;
        }

        BackpackWrapper wrapper = BackpackEntityHelpers.getInteractionWrapper(player, backpackStack);
        if (wrapper == null || !wrapper.canPlayerAccess(player.getUniqueID())) {
            return false;
        }

        ItemStack slotStack = slot.getStack();
        if (slotStack == null || slotStack.stackSize <= 0) {
            return false;
        }

        ItemStack extracted;
        if (result.isOutputSlot()) {
            extracted = slot.decrStackSize(slotStack.stackSize);
        } else {
            extracted = slot.decrStackSize(result.getInsertedCount());
        }
        if (extracted == null || extracted.stackSize <= 0) {
            return false;
        }

        slot.onPickupFromSlot(player, extracted);

        ItemStack remaining = wrapper.insertItem(extracted.copy(), false);
        if (remaining != null && remaining.stackSize > 0) {
            if (!player.inventory.addItemStackToInventory(remaining)) {
                player.dropPlayerItemWithRandomChoice(remaining, false);
            }
        }

        wrapper.writeToItem(player);
        player.inventory.setItemStack(backpackStack);
        player.inventory.markDirty();
        slot.onSlotChanged();
        syncContainer(container, player);
        return true;
    }

    private static boolean executeCursorIntoSlottedBackpack(Container container, EntityPlayer player, Slot slot,
        ItemStack cursorStack) {
        ItemStack backpackStack = slot.getStack();
        if (cursorStack == null || backpackStack == null
            || !BackpackEntityHelpers.isBackpackStack(backpackStack, false)) {
            return false;
        }

        BackpackWrapper wrapper = BackpackEntityHelpers.getInteractionWrapper(player, backpackStack);
        if (wrapper == null || !wrapper.canPlayerAccess(player.getUniqueID())) {
            return false;
        }

        ItemStack remaining = wrapper.insertItem(cursorStack.copy(), false);
        int remainingCount = remaining == null ? 0 : remaining.stackSize;
        if (remainingCount >= cursorStack.stackSize) {
            return false;
        }

        wrapper.writeToItem(player);
        player.inventory.setItemStack(remainingCount <= 0 ? null : remaining);
        player.inventory.markDirty();
        slot.putStack(backpackStack);
        slot.onSlotChanged();
        syncContainer(container, player);
        return true;
    }

    private static void syncContainer(Container container, EntityPlayer player) {
        container.detectAndSendChanges();
        if (player instanceof EntityPlayerMP playerMP) {
            playerMP.updateHeldItem();
            playerMP.inventoryContainer.detectAndSendChanges();
        }
    }
}
