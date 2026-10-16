package ruiseki.storagecore.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.inventory.ClickType;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.NEAAnimationHandler;
import com.cleanroommc.modularui.utils.Platform;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.StoragePanel;

public class StorageContainer extends ModularContainer {

    private static final int DROP_TO_WORLD = -999;
    private static final int LEFT_MOUSE = 0;
    private static final int RIGHT_MOUSE = 1;
    private static final int THROW = 4;

    protected final IStorageWrapper wrapper;
    protected final StoragePanel panel;

    public StorageContainer(IStorageWrapper wrapper, StoragePanel panel) {
        this.wrapper = wrapper;
        this.panel = panel;
    }

    public IStorageWrapper getWrapper() {
        return wrapper;
    }

    public StoragePanel getPanel() {
        return panel;
    }

    @Override
    public ItemStack slotClick(int slotId, int mouseButton, int mode, EntityPlayer player) {
        ClickType clickTypeIn = ClickType.fromNumber(mode);

        InventoryPlayer playerInventory = player.inventory;
        ItemStack heldStack = playerInventory.getItemStack();
        ItemStack returnable = null;

        // Handle click events in the inventory
        if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE)
            && (mouseButton == LEFT_MOUSE || mouseButton == RIGHT_MOUSE)) {

            // If the slot ID is DROP_TO_WORLD, delegate to the original slot click method
            if (slotId == DROP_TO_WORLD) {
                return superSlotClick(slotId, mouseButton, mode, player);
            }

            // Early return if the slot ID is invalid (< 0)
            if (slotId < 0) return Platform.EMPTY_STACK;

            // Handle QuickMove (shift-click)
            if (clickTypeIn == ClickType.QUICK_MOVE) {
                Slot fromSlot = getSlot(slotId);

                if (!fromSlot.canTakeStack(player)) {
                    return Platform.EMPTY_STACK;
                }

                if (NEAAnimationHandler.shouldHandleNEA(this)) {
                    returnable = NEAAnimationHandler.injectQuickMove(this, player, slotId, fromSlot);
                } else {
                    returnable = handleQuickMove(player, slotId, fromSlot);
                }
            } else { // Handle PICKUP click (left/right click)
                Slot clickedSlot = getSlot(slotId);

                if (clickedSlot != null) {
                    ItemStack slotStack = clickedSlot.getStack(); // Get the stack in the slot

                    // If the slot is empty
                    if (ItemHelpers.isEmpty(slotStack)) {
                        // If the player is holding an item and the slot accepts it
                        if (!ItemHelpers.isEmpty(heldStack) && clickedSlot.isItemValid(heldStack)) {
                            int stackCount = mouseButton == LEFT_MOUSE ? heldStack.stackSize : 1;

                            // Limit the stack size that can be placed in the slot
                            int lim = stackLimit(clickedSlot, heldStack);
                            if (stackCount > lim) {
                                stackCount = lim;
                            }

                            // Split the stack from the player's hand and put into the slot
                            ItemStack placed = heldStack.splitStack(stackCount);
                            updateSlotStack(clickedSlot, placed);

                            // If the player runs out of items in hand, set heldStack to null
                            if (heldStack.stackSize <= 0) {
                                playerInventory.setItemStack(null);
                            }
                        }
                    } else if (clickedSlot.canTakeStack(player)) { // Slot has items and can be taken
                        if (ItemHelpers.isEmpty(heldStack)) { // Player is not holding any item
                            int s = Math.min(slotStack.stackSize, slotStack.getMaxStackSize());
                            int toRemove = (mouseButton == LEFT_MOUSE) ? s : (s + 1) / 2;

                            // Extract item from slot into player's hand
                            ItemStack extracted = clickedSlot.decrStackSize(toRemove);

                            if (!ItemHelpers.isEmpty(extracted)) {
                                playerInventory.setItemStack(extracted);
                                clickedSlot.onPickupFromSlot(player, extracted);
                            }

                            if (ItemHelpers.isEmpty(clickedSlot.getStack())) {
                                clickedSlot.putStack(null);
                            }

                            clickedSlot.onSlotChanged();
                            detectAndSendChanges();

                            return extracted;
                        } else if (clickedSlot.isItemValid(heldStack)) { // Player is holding a valid item
                            // If the items are the same type, damage, and NBT
                            if (slotStack.getItem() == heldStack.getItem()
                                && slotStack.getItemDamage() == heldStack.getItemDamage()
                                && ItemStack.areItemStackTagsEqual(slotStack, heldStack)) {

                                int stackCount = mouseButton == LEFT_MOUSE ? heldStack.stackSize : 1;

                                // Limit the number of items that can be merged into the slot
                                int lim = stackLimit(clickedSlot, heldStack);
                                if (stackCount > lim - slotStack.stackSize) {
                                    stackCount = lim - slotStack.stackSize;
                                }

                                // Split items from player's hand
                                if (stackCount > 0) {
                                    heldStack.stackSize -= stackCount;

                                    if (heldStack.stackSize <= 0) {
                                        playerInventory.setItemStack(null);
                                    }

                                    ItemStack merged = slotStack.copy();
                                    merged.stackSize += stackCount;

                                    updateSlotStack(clickedSlot, merged);
                                }
                            } else if (heldStack.stackSize <= stackLimit(clickedSlot, heldStack)) {
                                clickedSlot.putStack(heldStack);
                                clickedSlot.onSlotChanged();
                                playerInventory.setItemStack(slotStack);
                            }
                        } else if (slotStack.getItem() == heldStack.getItem() && heldStack.getMaxStackSize() > 1
                            && (!slotStack.getHasSubtypes() || slotStack.getItemDamage() == heldStack.getItemDamage())
                            && ItemStack.areItemStackTagsEqual(slotStack, heldStack)) {

                                int stackCount = slotStack.stackSize;

                                // Merge slot stack into held stack if possible
                                if (stackCount > 0 && stackCount + heldStack.stackSize <= heldStack.getMaxStackSize()) {
                                    heldStack.stackSize += stackCount;
                                    slotStack = clickedSlot.decrStackSize(stackCount);

                                    if (slotStack.stackSize == 0) {
                                        clickedSlot.putStack(null);
                                    }

                                    clickedSlot.onPickupFromSlot(player, playerInventory.getItemStack());
                                }
                            }
                    }
                    clickedSlot.onSlotChanged();
                }
            }

            detectAndSendChanges(); // Update inventory on client
            return returnable;
        }
        return super.slotClick(slotId, mouseButton, mode, player);
    }

    private void updateSlotStack(Slot slot, ItemStack stack) {
        slot.putStack(stack);
        slot.onSlotChanged();
    }
}
