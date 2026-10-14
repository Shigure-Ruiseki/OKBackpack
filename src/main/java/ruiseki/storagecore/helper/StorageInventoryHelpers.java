package ruiseki.storagecore.helper;

import net.minecraft.item.ItemStack;

import ruiseki.backpack.Capabilities;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.capability.wrapper.PlayerMainInvWrapper;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.inventory.InventoryHandler;

public class StorageInventoryHelpers {

    public static void transferPlayerToStorage(IStorageWrapper wrapper, PlayerMainInvWrapper playerInv,
        boolean transferMatched) {
        InventoryHandler handler = wrapper.getInventoryHandler();
        for (int i = 9; i < playerInv.getSlots(); i++) {
            ItemStack stack = playerInv.getStackInSlot(i);
            if (ItemHelpers.isEmpty(stack)) continue;
            if (transferMatched && !hasMatchingSlot(wrapper, stack)) {
                continue;
            }
            IStorageWrapper stackWrapper = CapabilityHelpers.getCapability(stack, Capabilities.STORAGE_WRAPPER)
                .getOrNull();
            if (stackWrapper != null && stackWrapper.getContentsUuid()
                .equals(wrapper.getContentsUuid())) {
                continue;
            }

            ItemStack remaining = handler.insertItem(stack, false);
            playerInv.setStackInSlot(i, remaining);
        }
    }

    public static boolean hasMatchingSlot(IStorageWrapper wrapper, ItemStack stack) {
        InventoryHandler handler = wrapper.getInventoryHandler();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack inSlot = handler.getStackInSlot(i);
            if (ItemHandlerHelpers.canItemStacksStack(inSlot, stack)) {
                return true;
            }
        }
        return false;
    }

    public static void transferStorageToPlayer(IStorageWrapper wrapper, PlayerMainInvWrapper playerInv,
        boolean transferMatched) {
        InventoryHandler handler = wrapper.getInventoryHandler();
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);

            for (int j = 9; j < playerInv.getSlots(); j++) {
                if (transferMatched && ItemHelpers.isEmpty(playerInv.getStackInSlot(j))) continue;
                stack = playerInv.insertItem(j, stack, false);
            }

            handler.setStackInSlot(i, stack);
        }
    }
}
