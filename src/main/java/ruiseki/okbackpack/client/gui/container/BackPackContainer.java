package ruiseki.okbackpack.client.gui.container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import com.cleanroommc.modularui.api.inventory.ClickType;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.NEAAnimationHandler;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.IBackpackWrapper;
import ruiseki.okbackpack.api.IStorageContainer;
import ruiseki.okbackpack.api.upgrade.IUpgradeItem;
import ruiseki.okbackpack.api.wrapper.IToggleable;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.handler.IndexedInventoryCraftingWrapper;
import ruiseki.okbackpack.client.gui.slot.AnvilOutputModularSlot;
import ruiseki.okbackpack.client.gui.slot.IndexedModularCraftingMatrixSlot;
import ruiseki.okbackpack.client.gui.slot.IndexedModularCraftingSlot;
import ruiseki.okbackpack.client.gui.slot.ModularBackpackSlot;
import ruiseki.okbackpack.client.gui.slot.ModularUpgradeSlot;
import ruiseki.okbackpack.client.gui.slot.ModularUpgradeWidgetSlot;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.item.crafting.CraftingUpgradeWrapper;
import ruiseki.okbackpack.common.network.PacketBackpackNBT;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.compat.tic.TinkersHelpers;

public class BackPackContainer extends ModularContainer implements IStorageContainer<BackPackContainer> {

    public final IBackpackWrapper wrapper;
    protected final Integer backpackSlotIndex;

    private static final int DROP_TO_WORLD = -999;
    private static final int LEFT_MOUSE = 0;
    private static final int RIGHT_MOUSE = 1;

    private static final String PLAYER_INV = "player_inventory";

    protected final Map<Integer, IndexedInventoryCraftingWrapper> inventoryCraftingInstances = new HashMap<>();
    protected final Map<Integer, IndexedModularCraftingSlot> craftingSlotInstances = new HashMap<>();

    public BackPackContainer(IBackpackWrapper wrapper, Integer backpackSlotIndex) {
        this.wrapper = wrapper;
        this.backpackSlotIndex = backpackSlotIndex;
    }

    @Override
    public void registerSlot(String panelName, ModularSlot slot) {
        super.registerSlot(panelName, slot);
        if (slot instanceof IndexedModularCraftingSlot s) {
            registerCraftingSlot(s.getUpgradeSlotIndex(), s);
        }
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        if (inventoryIn instanceof IndexedInventoryCraftingWrapper inventoryCrafting) {

            EntityPlayer player = getPlayer();
            ItemStack result;

            // server-side compute recipe
            if (!getGuiData().isClient()) {
                if (Mods.TConstruct.isLoaded()) {
                    result = TinkersHelpers.getTinkersRecipe(inventoryCrafting);
                } else {
                    result = CraftingManager.getInstance()
                        .findMatchingRecipe(inventoryCrafting, player.worldObj);
                }

                // update result slot
                IndexedModularCraftingSlot slot = craftingSlotInstances.get(inventoryCrafting.getUpgradeSlotIndex());
                if (slot != null) {
                    slot.updateResult(result);
                }

                // set crafting slot server-side
                inventoryCrafting.setSlot(9, result, false);

                detectAndSendChanges();
            } else {
                // client: just display current result
                IndexedModularCraftingSlot slot = craftingSlotInstances.get(inventoryCrafting.getUpgradeSlotIndex());
                if (slot != null) {
                    slot.updateResult(inventoryCrafting.getStackInSlot(9));
                }
            }
        }
    }

    @Override
    public Slot getSlotFromInventory(IInventory inv, int slotIndex) {
        Slot slot = super.getSlotFromInventory(inv, slotIndex);
        if (slot != null) return slot;

        // Fallback: ModularSlot wrapping IItemHandler may not match via
        // SlotItemHandler.isSlotInInventory in some cases. Search by slot group.
        if (inv instanceof InventoryPlayer) {
            for (var s : this.inventorySlots) {
                if (s instanceof ModularSlot ms && PLAYER_INV.equals(ms.getSlotGroupName())
                    && ms.getSlotIndex() == slotIndex) {
                    return ms;
                }
            }
        }
        return null;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        // Server-side only: sync dirty changes to client
        if (!getGuiData().isClient() && wrapper.isDirty()) {
            EntityPlayer player = getPlayer();

            // Process any pending jukebox stops immediately
            if (wrapper instanceof BackpackWrapper bw) {
                bw.processPendingJukeboxStops(player);
            }

            // Write changes to the actual ItemStack (using UUID tracking)
            wrapper.writeToItem(player);

            // Clear dirty flag
            wrapper.markClean();

            // Send NBT update packet to client (only if backpack is valid)
            if (wrapper.getBackpack() != null && wrapper.getType() != null
                && backpackSlotIndex != null
                && player instanceof EntityPlayerMP playerMP) {

                OKBackpack.instance.getPacketHandler()
                    .sendToPlayer(
                        new PacketBackpackNBT(backpackSlotIndex, wrapper.getBackpackNBT(), wrapper.getType()),
                        playerMP);
            }
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);

        // Final sync before closing - ensure all changes are saved
        if (!getGuiData().isClient()) {
            // Process any pending jukebox stops before closing
            if (wrapper instanceof BackpackWrapper bw) {
                bw.processPendingJukeboxStops(player);
            }
            wrapper.writeToItem(player);
            wrapper.markClean();
        }
    }

    @Override
    public ItemStack slotClick(int slotId, int mouseButton, int mode, EntityPlayer player) {
        ClickType clickTypeIn = ClickType.fromNumber(mode);

        InventoryPlayer playerInventory = player.inventory;
        ItemStack heldStack = playerInventory.getItemStack();
        ItemStack returnable = null;

        if (clickTypeIn == ClickType.QUICK_CRAFT || acc().getDragEvent() != 0) {
            return super.slotClick(slotId, mouseButton, mode, player);
        }

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

                // If the slot cannot be taken from, return empty
                if (!fromSlot.canTakeStack(player)) {
                    return Platform.EMPTY_STACK;
                }

                // Check if NEA animation should handle this QuickMove
                if (NEAAnimationHandler.shouldHandleNEA(this)) {
                    returnable = NEAAnimationHandler.injectQuickMove(this, player, slotId, fromSlot);
                } else {
                    // Default QuickMove handling
                    returnable = handleQuickMove(player, slotId, fromSlot);
                }

            } else { // Handle PICKUP click (left/right click)
                Slot clickedSlot = getSlot(slotId);

                if (clickedSlot != null) {
                    ItemStack slotStack = clickedSlot.getStack(); // Get the stack in the slot

                    // If the slot is empty
                    if (slotStack == null) {
                        // If the player is holding an item and the slot accepts it
                        if (heldStack != null && clickedSlot.isItemValid(heldStack)) {
                            int stackCount = mouseButton == LEFT_MOUSE ? heldStack.stackSize : 1;

                            // Limit the stack size that can be placed in the slot
                            int lim = stackLimit(clickedSlot, heldStack);
                            if (stackCount > lim) {
                                stackCount = lim;
                            }

                            // Split the stack from the player's hand and put into the slot
                            clickedSlot.putStack(heldStack.splitStack(stackCount));

                            // If the player runs out of items in hand, set heldStack to null
                            if (heldStack.stackSize == 0) {
                                playerInventory.setItemStack(null);
                            }
                        }

                    } else if (clickedSlot.canTakeStack(player)) { // Slot has items and can be taken
                        if (heldStack == null) { // Player is not holding any item
                            int s = Math.min(slotStack.stackSize, slotStack.getMaxStackSize());
                            int toRemove = (mouseButton == LEFT_MOUSE) ? s : (s + 1) / 2;

                            // Extract item from slot into player's hand
                            ItemStack extracted = clickedSlot.decrStackSize(toRemove);

                            if (extracted != null) {
                                playerInventory.setItemStack(extracted);
                                clickedSlot.onPickupFromSlot(player, extracted);
                            }

                            // If the slot is empty after extraction, set it to null
                            if (clickedSlot.getStack() == null || clickedSlot.getStack().stackSize <= 0) {
                                clickedSlot.putStack(null);
                            }

                            clickedSlot.onSlotChanged();
                            detectAndSendChanges();

                            return extracted == null ? Platform.EMPTY_STACK : extracted;

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
                                heldStack.splitStack(stackCount);

                                if (heldStack.stackSize == 0) {
                                    playerInventory.setItemStack(null);
                                }

                                // Merge into the slot stack
                                slotStack.stackSize += stackCount;
                                clickedSlot.putStack(slotStack);

                            } else if (heldStack.stackSize <= stackLimit(clickedSlot, heldStack)) {
                                if (clickedSlot instanceof ModularUpgradeSlot mus) {
                                    int slotIndex = clickedSlot.getSlotIndex();
                                    ((IItemHandlerModifiable) mus.getItemHandler())
                                        .setStackInSlot(slotIndex, heldStack);
                                    clickedSlot.onSlotChanged();
                                } else {
                                    clickedSlot.putStack(heldStack);
                                    clickedSlot.onSlotChanged();
                                }

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
        } else if (clickTypeIn == ClickType.PICKUP_ALL) {

            if (heldStack != null) {

                int start = mouseButton == 0 ? 0 : inventorySlots.size() - 1;
                int step = mouseButton == 0 ? 1 : -1;

                for (int pass = 0; pass < 2; pass++) {
                    for (int i = start; i >= 0 && i < inventorySlots.size()
                        && heldStack.stackSize < heldStack.getMaxStackSize(); i += step) {

                        Slot slot1 = inventorySlots.get(i);

                        if (!(slot1 instanceof Slot)) continue;

                        if (slot1 instanceof ModularSlot && ((ModularSlot) slot1).isPhantom()) continue;
                        if (slot1 instanceof IndexedModularCraftingSlot) continue;
                        if (slot1 instanceof IndexedModularCraftingMatrixSlot slot) {
                            IUpgradeWrapper wrapper = this.wrapper.getUpgradeHandler()
                                .getSlotWrappers()
                                .get(slot.getSlotIndex());
                            if (wrapper instanceof IToggleable toggleable && !toggleable.isEnabled()) continue;
                        }

                        if (slot1.getHasStack() && slot1.canTakeStack(player) && canMergeSlot(heldStack, slot1)) {

                            ItemStack stackInSlot = slot1.getStack();

                            if (pass != 0 || stackInSlot.stackSize != heldStack.getMaxStackSize()) {
                                int take = Math
                                    .min(heldStack.getMaxStackSize() - heldStack.stackSize, stackInSlot.stackSize);

                                ItemStack removed = slot1.decrStackSize(take);

                                if (removed != null) {
                                    heldStack.stackSize += removed.stackSize;
                                    slot1.onPickupFromSlot(player, removed);
                                }

                                if (slot1.getStack() == null || slot1.getStack().stackSize <= 0) {
                                    slot1.putStack(null);
                                }
                            }
                        }
                    }
                }
            }

            detectAndSendChanges();
            return Platform.EMPTY_STACK;
        }
        // creative clone
        else if (clickTypeIn == ClickType.CLONE && player.capabilities.isCreativeMode
            && (heldStack == null)
            && slotId >= 0) {

                Slot slot = getSlot(slotId);

                if (slot != null && slot.getHasStack()) {
                    ItemStack copy = slot.getStack()
                        .copy();
                    copy.stackSize = copy.getMaxStackSize();
                    playerInventory.setItemStack(copy);
                }

                return Platform.EMPTY_STACK;
            }
        // hotbar swap blocked for backpack slot
        else if (clickTypeIn == ClickType.SWAP && mouseButton >= 0
            && mouseButton < 9
            && backpackSlotIndex != null
            && backpackSlotIndex == mouseButton) {

                return Platform.EMPTY_STACK;
            }

        return super.slotClick(slotId, mouseButton, mode, player);
    }

    @Override
    public ItemStack transferItem(ModularSlot fromSlot, ItemStack fromStack) {
        if (fromStack == null || fromStack.stackSize <= 0) return null;
        int originalSize = fromStack.stackSize;

        if (fromSlot instanceof IndexedModularCraftingSlot craftingSlot) {
            IndexedInventoryCraftingWrapper inventoryCrafting = inventoryCraftingInstances
                .get(craftingSlot.getUpgradeSlotIndex());

            if (inventoryCrafting == null) {
                transferItemFiltered(fromSlot, fromStack, slot -> PLAYER_INV.equals(slot.getSlotGroupName()));
            } else
                if (inventoryCrafting.getCraftingDestination() == CraftingUpgradeWrapper.CraftingDestination.BACKPACK) {

                    transferItemFiltered(
                        fromSlot,
                        fromStack,
                        slot -> slot instanceof ModularBackpackSlot && wrapper.isSlotMemorized(slot.getSlotIndex()),
                        slot -> slot instanceof ModularBackpackSlot);
                } else {
                    transferItemFiltered(fromSlot, fromStack, slot -> PLAYER_INV.equals(slot.getSlotGroupName()));
                }
        }
        if (fromSlot instanceof ModularUpgradeSlot upgradeSlot) {
            transferItemFiltered(
                fromSlot,
                fromStack,
                slot -> PLAYER_INV.equals(slot.getSlotGroupName()),
                slot -> slot instanceof ModularBackpackSlot && wrapper.isSlotMemorized(slot.getSlotIndex()),
                slot -> slot instanceof ModularBackpackSlot);
            if (fromStack.stackSize != originalSize) {
                int slotIndex = upgradeSlot.getSlotIndex();
                ((IItemHandlerModifiable) upgradeSlot.getItemHandler()).setStackInSlot(slotIndex, Platform.EMPTY_STACK);
            }
            return fromStack;
        } else if (PLAYER_INV.equals(fromSlot.getSlotGroupName())) {
            if (fromStack.getItem() instanceof IUpgradeItem) {
                transferItemFiltered(fromSlot, fromStack, slot -> slot instanceof ModularUpgradeSlot);
            }
            if (fromStack.stackSize == originalSize) {
                if (wrapper.isShiftClickIntoOpenTab()) {
                    transferItemFiltered(
                        fromSlot,
                        fromStack,
                        this::isUpgradeWidgetTargetSlot,
                        slot -> slot instanceof ModularBackpackSlot && wrapper.isSlotMemorized(slot.getSlotIndex()),
                        slot -> slot instanceof ModularBackpackSlot);
                } else {
                    transferItemFiltered(
                        fromSlot,
                        fromStack,
                        slot -> slot instanceof ModularBackpackSlot && wrapper.isSlotMemorized(slot.getSlotIndex()),
                        slot -> slot instanceof ModularBackpackSlot,
                        this::isUpgradeWidgetTargetSlot);
                }
            }
        } else {
            return super.transferItem(fromSlot, fromStack);
        }

        if (fromStack.stackSize != originalSize) {
            return fromStack;
        }

        return super.transferItem(fromSlot, fromStack);
    }

    @SafeVarargs
    public final void transferItemFiltered(ModularSlot fromSlot, ItemStack fromStack,
        Predicate<ModularSlot>... slotFilters) {
        SlotGroup fromSlotGroup = fromSlot.getSlotGroup();

        for (Predicate<ModularSlot> slotFilter : slotFilters) {

            List<ModularSlot> targets = this.inventorySlots.stream()
                .filter(slot -> slot instanceof ModularSlot)
                .map(slot -> (ModularSlot) slot)
                .filter(slotFilter)
                .collect(Collectors.toList());

            for (ModularSlot toSlot : targets) {
                if (fromStack.stackSize <= 0) break;
                if (toSlot.getSlotGroup() != fromSlotGroup) {
                    transferToSlot(fromSlot, toSlot, fromStack);
                }
            }
        }

    }

    private boolean isUpgradeWidgetTargetSlot(ModularSlot slot) {
        if (!(slot instanceof ModularUpgradeWidgetSlot uwSlot)) return false;
        int openIndex = getOpenUpgradeSlotIndex();
        if (openIndex < 0 || uwSlot.getUpgradeSlotIndex() != openIndex) return false;
        if (slot.isPhantom()) return false;
        if (isExtractionOnlyUpgradeSlot(slot)) return false;
        return true;
    }

    private int getOpenUpgradeSlotIndex() {
        for (int i = 0; i < wrapper.getUpgradeHandler()
            .getSlots(); i++) {
            IUpgradeWrapper upgradeWrapper = wrapper.getUpgradeHandler()
                .getWrapperInSlot(i);
            if (upgradeWrapper != null && upgradeWrapper.isTabOpened()) {
                return i;
            }
        }
        return -1;
    }

    private boolean isExtractionOnlyUpgradeSlot(ModularSlot slot) {
        if (slot instanceof IndexedModularCraftingSlot || slot instanceof AnvilOutputModularSlot) {
            return true;
        }
        String slotGroupName = slot.getSlotGroupName();
        return slotGroupName != null && slotGroupName.startsWith("smelting_slots_") && slot.getSlotIndex() == 2;
    }

    private boolean canQuickMoveIntoSlot(ModularSlot toSlot, ItemStack fromStack) {
        if (toSlot.isPhantom() || isExtractionOnlyUpgradeSlot(toSlot) || !toSlot.isItemValid(fromStack)) {
            return false;
        }
        if (toSlot instanceof ModularUpgradeWidgetSlot uwSlot && !uwSlot.canShiftClickInsert(fromStack)) {
            return false;
        }

        ItemStack toStack = toSlot.getStack();
        if (toStack == null) {
            return true;
        }

        return ItemHandlerHelper.canItemStacksStack(fromStack, toStack)
            && toStack.stackSize < stackLimit(toSlot, fromStack);
    }

    protected void transferToSlot(ModularSlot fromSlot, ModularSlot toSlot, ItemStack fromStack) {

        if (!canQuickMoveIntoSlot(toSlot, fromStack)) {
            return;
        }

        boolean isBackpackSlot = toSlot instanceof ModularBackpackSlot;
        ItemStack toStack = toSlot.getStack();

        int limit = stackLimit(toSlot, fromStack);

        if (isBackpackSlot) {
            int slotIndex = toSlot.getSlotIndex();

            if (wrapper.isSlotMemorized(slotIndex)) {

                ItemStack memory = wrapper.getMemoryStack(slotIndex);

                if (memory != null) {

                    boolean match = wrapper.isMemoryStackRespectNBT(slotIndex)
                        ? ItemStack.areItemStacksEqual(memory, fromStack)
                        : fromStack.isItemEqual(memory);

                    if (!match) {
                        return;
                    }
                }
            }
        }

        // merge stack
        if (fromStack.stackSize > 0 && !fromSlot.isPhantom()
            && toStack != null
            && ItemHandlerHelper.canItemStacksStack(fromStack, toStack)) {

            int j = toStack.stackSize + fromStack.stackSize;

            if (j <= limit) {
                fromStack.stackSize = 0;
                toStack.stackSize = j;
            } else {

                fromStack.stackSize -= limit - toStack.stackSize;
                toStack.stackSize = limit;
            }

            toSlot.putStack(toStack);
            toSlot.onSlotChanged();
        }

        // empty slot
        if (fromStack.stackSize > 0 && toStack == null) {

            int move = Math.min(fromStack.stackSize, limit);

            toSlot.putStack(fromStack.splitStack(move));
            toSlot.onSlotChanged();
        }
    }

    @Override
    public BackPackContainer getContainer() {
        return this;
    }

    @Override
    public void registerCraftingSlot(int slotIndex, IndexedModularCraftingSlot craftingSlot) {

        craftingSlotInstances.put(slotIndex, craftingSlot);

        IndexedInventoryCraftingWrapper wrapper = inventoryCraftingInstances.get(slotIndex);

        if (wrapper != null) {
            craftingSlot.setCraftMatrix(wrapper);
        }
    }

    @Override
    public void registerInventoryCrafting(int slotIndex, IndexedInventoryCraftingWrapper inventoryCrafting) {

        inventoryCraftingInstances.put(slotIndex, inventoryCrafting);

        IndexedModularCraftingSlot slot = craftingSlotInstances.get(slotIndex);

        if (slot != null) {
            slot.setCraftMatrix(inventoryCrafting);
        }
    }

    private boolean canMergeSlot(ItemStack stack, Slot slot) {
        if (slot == null || stack == null) return false;

        if (!slot.getHasStack()) return true;

        ItemStack slotStack = slot.getStack();

        return ItemHandlerHelper.canItemStacksStack(stack, slotStack);
    }
}
