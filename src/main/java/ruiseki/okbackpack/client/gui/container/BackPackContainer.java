package ruiseki.okbackpack.client.gui.container;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.cleanroommc.modularui.widgets.slot.ModularCraftingSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.wrapper.UpgradeWrapperFactory;
import ruiseki.okbackpack.client.gui.handler.IndexedInventoryCraftingWrapper;
import ruiseki.okbackpack.client.gui.slot.IndexedModularCraftingMatrixSlot;
import ruiseki.okbackpack.client.gui.slot.IndexedModularCraftingSlot;
import ruiseki.okbackpack.client.gui.slot.ModularBackpackSlot;
import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.item.wrapper.CraftingUpgradeWrapper;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperBase;
import ruiseki.okbackpack.common.network.PacketBackpackNBT;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.compat.tic.TinkersHelpers;

public class BackPackContainer extends ModularContainer {

    public final BackpackWrapper wrapper;
    protected final Integer backpackSlotIndex;

    private static final int DROP_TO_WORLD = -999;
    private static final int LEFT_MOUSE = 0;
    private static final int RIGHT_MOUSE = 1;

    private int dragState = 0;
    private final Set<Slot> dragSlots = new HashSet<>();
    private int dragButton = -1;

    private static final String PLAYER_INV = "player_inventory";

    protected final Map<Integer, IndexedInventoryCraftingWrapper> inventoryCraftingInstances = new HashMap<>();
    protected final Map<Integer, IndexedModularCraftingSlot> craftingSlotInstances = new HashMap<>();

    public BackPackContainer(BackpackWrapper wrapper, Integer backpackSlotIndex) {
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

        if (!getGuiData().isClient() && inventoryIn instanceof IndexedInventoryCraftingWrapper inventoryCrafting) {

            EntityPlayerMP playerMP = (EntityPlayerMP) getPlayer();

            ItemStack result;
            if (Mods.TConstruct.isLoaded()) {
                result = TinkersHelpers.getTinkersRecipe(inventoryCrafting);
            } else {
                result = CraftingManager.getInstance()
                    .findMatchingRecipe(inventoryCrafting, playerMP.worldObj);
            }

            IndexedModularCraftingSlot slot = craftingSlotInstances.get(inventoryCrafting.getUpgradeSlotIndex());

            if (slot != null) {
                slot.updateResult(result);
            }

            inventoryCrafting.setSlot(9, result, false);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        // Server-side only: sync dirty changes to client
        if (!getGuiData().isClient() && wrapper.isDirty) {
            EntityPlayer player = getPlayer();

            // Write changes to the actual ItemStack (using UUID tracking)
            wrapper.writeToItem(player);

            // Send NBT update packet to client (only if backpack is valid)
            if (wrapper.backpack != null && wrapper.type != null
                && backpackSlotIndex != null
                && player instanceof EntityPlayerMP playerMP) {

                // Clear dirty flag
                wrapper.clearDirty();

                OKBackpack.instance.getPacketHandler()
                    .sendToPlayer(
                        new PacketBackpackNBT(backpackSlotIndex, wrapper.getTagCompound(), wrapper.type),
                        playerMP);
            }
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);

        // Final sync before closing - ensure all changes are saved
        if (!getGuiData().isClient()) {
            wrapper.writeToItem(player);
            wrapper.clearDirty();
        }
    }

    @Override
    public ItemStack slotClick(int slotId, int mouseButton, int mode, EntityPlayer player) {
        ClickType clickTypeIn = ClickType.fromNumber(mode);

        InventoryPlayer inventoryplayer = player.inventory;
        ItemStack heldStack = inventoryplayer.getItemStack();
        ItemStack returnable = null;

        if (clickTypeIn == ClickType.QUICK_CRAFT) {
            return handleQuickCraft(slotId, mouseButton, mode, player);
        } else if (this.dragState != 0) {
            this.resetDrag();
        }

        if (clickTypeIn == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot clicked = getSlot(slotId);
            ItemStack cursor = inventoryplayer.getItemStack();
            if (cursor != null && (clicked == null || !clicked.getHasStack() || !clicked.canTakeStack(player))) {
                int start = mouseButton == 0 ? 0 : this.inventorySlots.size() - 1;
                int step = mouseButton == 0 ? 1 : -1;

                for (int pass = 0; pass < 2; pass++) {
                    for (int i = start; i >= 0 && i < this.inventorySlots.size()
                        && cursor.stackSize < cursor.getMaxStackSize(); i += step) {
                        Slot slot = this.inventorySlots.get(i);

                        if (slot == null || !slot.getHasStack()) continue;
                        if (slot instanceof ModularFilterSlot) continue;
                        if (slot instanceof ModularCraftingSlot) continue;

                        if (slot instanceof IndexedModularCraftingMatrixSlot matrixSlot) {
                            ItemStack stack = wrapper.upgradeHandler.getStackInSlot(matrixSlot.getUpgradeSlotIndex());
                            UpgradeWrapperBase upgradeWrapper = UpgradeWrapperFactory
                                .createWrapper(stack, this.wrapper);
                            if (upgradeWrapper == null) continue;
                            if (!upgradeWrapper.isTabOpened()) continue;
                        }

                        ItemStack slotStack = slot.getStack();

                        if (func_94527_a(slot, cursor, true) && slot.canTakeStack(player)
                            && func_94530_a(cursor, slot)
                            && (pass != 0 || slotStack.stackSize != slotStack.getMaxStackSize())) {
                            int take = Math.min(cursor.getMaxStackSize() - cursor.stackSize, slotStack.stackSize);

                            ItemStack removed = slot.decrStackSize(take);
                            cursor.stackSize += take;

                            if (removed.stackSize <= 0) slot.putStack(Platform.EMPTY_STACK);

                            slot.onPickupFromSlot(player, removed);
                        }
                    }
                }
            }

            detectAndSendChanges();
            return Platform.EMPTY_STACK;
        }

        if ((clickTypeIn == ClickType.PICKUP || clickTypeIn == ClickType.QUICK_MOVE)
            && (mouseButton == LEFT_MOUSE || mouseButton == RIGHT_MOUSE)) {

            if (slotId == DROP_TO_WORLD) {
                return superSlotClick(slotId, mouseButton, mode, player);
            }

            // early return
            if (slotId < 0) return Platform.EMPTY_STACK;

            if (clickTypeIn == ClickType.QUICK_MOVE) {
                Slot fromSlot = getSlot(slotId);

                if (fromSlot == null || !fromSlot.canTakeStack(player)) return Platform.EMPTY_STACK;

                if (fromSlot instanceof ModularFilterSlot) {
                    fromSlot.putStack(Platform.EMPTY_STACK);
                    return Platform.EMPTY_STACK;
                }

                if (NEAAnimationHandler.shouldHandleNEA(this)) {
                    returnable = NEAAnimationHandler.injectQuickMove(this, player, slotId, fromSlot);
                } else {
                    returnable = handleQuickMove(player, slotId, fromSlot);
                }
            } else {
                // PICKUP (normal left/right click)
                Slot clickedSlot = getSlot(slotId);
                boolean isBackpackSlot = clickedSlot instanceof ModularBackpackSlot;
                boolean isFilterSlot = clickedSlot instanceof ModularFilterSlot;
                boolean isCraftingSlot = clickedSlot instanceof ModularCraftingSlot;

                if (clickedSlot != null) {
                    ItemStack slotStack = clickedSlot.getStack();

                    if (isFilterSlot) {
                        if (heldStack != null) {
                            ItemStack putStack = heldStack.copy();
                            putStack.stackSize = 1;
                            clickedSlot.putStack(putStack);
                        }

                        if (heldStack == null) {
                            clickedSlot.putStack(Platform.EMPTY_STACK);
                        }

                        return Platform.EMPTY_STACK;
                    }

                    // If slot is empty & player holds something -> try place
                    if (slotStack == null) {
                        if (heldStack != null && clickedSlot.isItemValid(heldStack)) {
                            if (isCraftingSlot) {
                                return Platform.EMPTY_STACK;
                            }

                            int lim = stackLimit(clickedSlot, heldStack);
                            if (lim <= 0) return Platform.EMPTY_STACK;
                            int placeCount = mouseButton == LEFT_MOUSE ? heldStack.stackSize : 1;
                            if (placeCount > lim) placeCount = lim;

                            ItemStack toPut = heldStack.splitStack(placeCount);
                            clickedSlot.putStack(toPut);
                            clickedSlot.onPickupFromSlot(player, clickedSlot.getStack());

                            if (heldStack.stackSize <= 0) inventoryplayer.setItemStack(null);
                        }
                    }
                    // Slot has item and can be taken
                    else if (clickedSlot.canTakeStack(player)) {

                        // Player not holding anything => pick up (left: all/ right: half)
                        if (heldStack == null) {
                            int available = Math.min(slotStack.stackSize, slotStack.getMaxStackSize());
                            int take = mouseButton == LEFT_MOUSE ? available : (available + 1) / 2;

                            ItemStack taken = clickedSlot.decrStackSize(take);
                            inventoryplayer.setItemStack(taken);

                            if (clickedSlot.getStack() == null || clickedSlot.getStack().stackSize <= 0) {
                                clickedSlot.putStack(Platform.EMPTY_STACK);
                            }

                            clickedSlot.onPickupFromSlot(player, taken);
                        }
                        // Player holding something
                        else {

                            // If same item type and tags -> try merge into slot
                            if (slotStack.getItem() == heldStack.getItem()
                                && slotStack.getItemDamage() == heldStack.getItemDamage()
                                && ItemStack.areItemStackTagsEqual(slotStack, heldStack)
                                && !isCraftingSlot) {

                                int lim = stackLimit(clickedSlot, heldStack);
                                int canAdd = Math.max(0, lim - slotStack.stackSize);

                                if (canAdd > 0) {

                                    // Merge
                                    int want = mouseButton == LEFT_MOUSE ? heldStack.stackSize : 1;
                                    if (want > canAdd) want = canAdd;

                                    if (want > 0) {
                                        heldStack.splitStack(want);
                                        if (heldStack.stackSize == 0) inventoryplayer.setItemStack(null);

                                        slotStack.stackSize += want;
                                        clickedSlot.putStack(slotStack);
                                    }
                                }
                            }

                            // If heldStack fits entirely into slot (and is valid) -> swap
                            else if (clickedSlot.isItemValid(heldStack)
                                && heldStack.stackSize <= stackLimit(clickedSlot, heldStack)) {
                                    // swap: put held into slot, give slotStack to cursor
                                    clickedSlot.putStack(heldStack);
                                    inventoryplayer.setItemStack(slotStack);
                                }
                            // If items are different but stackable into heldStack (merge from slot into held)
                            else if (heldStack.getItem() == slotStack.getItem() && heldStack.getMaxStackSize() > 1
                                && (!slotStack.getHasSubtypes()
                                    || slotStack.getItemDamage() == heldStack.getItemDamage())
                                && ItemStack.areItemStackTagsEqual(slotStack, heldStack)) {
                                    int canMove = slotStack.stackSize;
                                    if (canMove > 0 && canMove + heldStack.stackSize <= heldStack.getMaxStackSize()) {
                                        heldStack.stackSize += canMove;
                                        ItemStack removed = clickedSlot.decrStackSize(canMove);
                                        if (removed.stackSize == 0) {
                                            clickedSlot.putStack(Platform.EMPTY_STACK);
                                        } else {
                                            clickedSlot.putStack(removed);
                                        }
                                        clickedSlot.onPickupFromSlot(player, inventoryplayer.getItemStack());
                                    }
                                }
                        }
                    }

                    clickedSlot.onSlotChanged();
                }
            }

            detectAndSendChanges();
            return returnable;
        }
        // creative clone
        else if (clickTypeIn == ClickType.CLONE && player.capabilities.isCreativeMode
            && (heldStack == null || heldStack.stackSize <= 0)
            && slotId >= 0) {

                Slot slot = getSlot(slotId);
                if (slot != null && slot.getHasStack()) {
                    ItemStack stack = slot.getStack()
                        .copy();
                    stack.stackSize = stack.getMaxStackSize();
                    player.inventory.setItemStack(stack);
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

                ItemStack memory = wrapper.getMemorizedStack(slotIndex);

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

    protected ItemStack handleQuickCraft(int slotId, int mouseButton, int mode, EntityPlayer player) {

        InventoryPlayer inv = player.inventory;
        ItemStack held = inv.getItemStack();
        ItemStack returnable = null;

        int previousState = dragState;

        this.dragState = func_94532_c(mouseButton);
        if ((previousState != 1 || this.dragState != 2) && previousState != this.dragState) {
            this.resetDrag();
        } else if (held == null) {
            this.resetDrag();
        } else if (this.dragState == 0) {
            this.dragButton = func_94529_b(mouseButton);

            if (func_94528_d(this.dragButton)) {
                this.dragState = 1;
                this.dragSlots.clear();
            } else {
                this.resetDrag();
            }
        } else if (this.dragState == 1) {
            Slot slot = getSlot(slotId);

            if (slot != null && func_94527_a(slot, held, true)
                && slot.isItemValid(held)
                && held.stackSize > this.dragSlots.size()
                && this.canDragIntoSlot(slot)) {
                this.dragSlots.add(slot);
            }
        } else if (this.dragState == 2) {
            if (!dragSlots.isEmpty()) {
                returnable = held.copy();
                int remainingAmount = held.stackSize;

                int perSlot;
                if (dragButton == 0) {
                    perSlot = held.stackSize / dragSlots.size();
                } else {
                    perSlot = 1;
                }
                if (perSlot <= 0) {
                    perSlot = 1;
                }

                for (Slot slot : dragSlots) {
                    if (slot == null || !func_94527_a(slot, held, true)
                        || !slot.isItemValid(held)
                        || !canDragIntoSlot(slot)) continue;

                    ItemStack slotStack = slot.getStack();
                    int before = slotStack == null ? 0 : slotStack.stackSize;

                    int limit = stackLimit(slot, held); // <-- Sử dụng stackLimit ở đây
                    int add = Math.min(perSlot, limit - before);
                    if (add <= 0) continue;

                    ItemStack newStack;
                    if (slot instanceof ModularFilterSlot) {
                        newStack = held.copy();
                        newStack.stackSize = 1;
                    } else {
                        newStack = held.copy();
                        newStack.stackSize = before + add;
                        remainingAmount -= add;
                    }

                    slot.putStack(newStack);
                    slot.onSlotChanged();
                }

                if (remainingAmount <= 0) returnable = null;
                else returnable.stackSize = remainingAmount;

                inv.setItemStack(returnable);
            }

            resetDrag();
        } else {
            this.resetDrag();
        }

        detectAndSendChanges();
        return Platform.EMPTY_STACK;
    }

    protected void resetDrag() {
        this.dragState = 0;
        this.dragSlots.clear();
    }

    private void registerCraftingSlot(int slotIndex, IndexedModularCraftingSlot craftingSlot) {

        craftingSlotInstances.put(slotIndex, craftingSlot);

        IndexedInventoryCraftingWrapper wrapper = inventoryCraftingInstances.get(slotIndex);

        if (wrapper != null) {
            craftingSlot.setCraftMatrix(wrapper);
        }
    }

    public void registerInventoryCrafting(int slotIndex, IndexedInventoryCraftingWrapper inventoryCrafting) {

        inventoryCraftingInstances.put(slotIndex, inventoryCrafting);

        IndexedModularCraftingSlot slot = craftingSlotInstances.get(slotIndex);

        if (slot != null) {
            slot.setCraftMatrix(inventoryCrafting);
        }
    }
}
