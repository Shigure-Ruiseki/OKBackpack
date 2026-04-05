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
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.IBackpackWrapper;
import ruiseki.okbackpack.api.IStorageContainer;
import ruiseki.okbackpack.client.gui.handler.IndexedInventoryCraftingWrapper;
import ruiseki.okbackpack.client.gui.slot.IndexedModularCraftingSlot;
import ruiseki.okbackpack.client.gui.slot.ModularBackpackSlot;
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

            // Send NBT update packet to client (only if backpack is valid)
            if (wrapper.getBackpack() != null && wrapper.getType() != null
                && backpackSlotIndex != null
                && player instanceof EntityPlayerMP playerMP) {

                // Clear dirty flag
                wrapper.markClean();

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

        // PICKUP
        if (clickTypeIn == ClickType.PICKUP && (mouseButton == LEFT_MOUSE || mouseButton == RIGHT_MOUSE)
            && slotId != DROP_TO_WORLD
            && slotId >= 0) {

            Slot clickedSlot = getSlot(slotId);
            if (clickedSlot == null) return Platform.EMPTY_STACK;

            ItemStack slotStack = clickedSlot.getStack();

            if (clickedSlot instanceof ModularBackpackSlot && slotStack != null && heldStack == null) {

                int limit = stackLimit(clickedSlot, slotStack);
                int s = Math.min(slotStack.stackSize, limit);

                int toRemove = (mouseButton == LEFT_MOUSE) ? s : (s + 1) / 2;

                ItemStack extracted = clickedSlot.decrStackSize(toRemove);

                if (extracted != null) {
                    playerInventory.setItemStack(extracted);
                    clickedSlot.onPickupFromSlot(player, extracted);
                }

                if (clickedSlot.getStack() == null || clickedSlot.getStack().stackSize <= 0) {
                    clickedSlot.putStack(null);
                }

                clickedSlot.onSlotChanged();
                detectAndSendChanges();

                return Platform.EMPTY_STACK;
            }
        }

        // PICKUP_ALL
        else if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot clickedSlot = getSlot(slotId);
            if (clickedSlot == null) return Platform.EMPTY_STACK;

            ItemStack slotStack = clickedSlot.getStack();
            int maxStackSize = stackLimit(clickedSlot, slotStack);

            if (heldStack != null && (!clickedSlot.getHasStack() || !clickedSlot.canTakeStack(player))) {

                int start = mouseButton == 0 ? 0 : inventorySlots.size() - 1;
                int step = mouseButton == 0 ? 1 : -1;

                for (int pass = 0; pass < 2; pass++) {
                    for (int i = start; i >= 0 && i < inventorySlots.size()
                        && heldStack.stackSize < maxStackSize; i += step) {

                        Slot slot1 = inventorySlots.get(i);

                        if (!(slot1 instanceof Slot)) continue;

                        if (slot1 instanceof ModularSlot && ((ModularSlot) slot1).isPhantom()) continue;

                        if (slot1.getHasStack() && func_94527_a(slot1, heldStack, true)
                            && slot1.canTakeStack(player)
                            && canMergeSlot(heldStack, slot1)) {

                            ItemStack stackInSlot = slot1.getStack();

                            if (pass != 0 || stackInSlot.stackSize != maxStackSize) {
                                int take = Math.min(maxStackSize - heldStack.stackSize, stackInSlot.stackSize);

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

        return superSlotClick(slotId, mouseButton, mode, player);
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
        } else if (PLAYER_INV.equals(fromSlot.getSlotGroupName())) {
            transferItemFiltered(
                fromSlot,
                fromStack,
                slot -> slot instanceof ModularBackpackSlot && wrapper.isSlotMemorized(slot.getSlotIndex()));
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

            List<ModularSlot> targets = getShiftClickSlots().stream()
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

    protected void transferToSlot(ModularSlot fromSlot, ModularSlot toSlot, ItemStack fromStack) {

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
