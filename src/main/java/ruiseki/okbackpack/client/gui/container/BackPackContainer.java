package ruiseki.okbackpack.client.gui.container;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.inventory.ClickType;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.NEAAnimationHandler;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.client.gui.slot.BackpackCraftingSlot;
import ruiseki.okbackpack.client.gui.slot.ModularBackpackSlot;
import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.item.wrapper.ICraftingUpgrade;
import ruiseki.okbackpack.common.item.wrapper.IVoidUpgrade;

public class BackPackContainer extends ModularContainer {

    private final BackpackWrapper wrapper;
    private final Integer backpackSlotIndex;

    private static final int DROP_TO_WORLD = -999;
    private static final int LEFT_MOUSE = 0;
    private static final int RIGHT_MOUSE = 1;

    private int dragState = 0;
    private final Set<Slot> dragSlots = new HashSet<>();
    private int dragButton = -1;

    public BackPackContainer(BackpackWrapper wrapper, Integer backpackSlotIndex) {
        this.wrapper = wrapper;
        this.backpackSlotIndex = backpackSlotIndex;
    }

    @Override
    public ItemStack slotClick(int slotId, int mouseButton, int mode, EntityPlayer player) {
        ClickType clickTypeIn = ClickType.fromNumber(mode);

        InventoryPlayer inventoryplayer = player.inventory;
        ItemStack heldStack = inventoryplayer.getItemStack();
        ItemStack returnable = null;

        if (clickTypeIn == ClickType.QUICK_CRAFT) {
            if (heldStack != null && slotId >= 0) {
                Slot clickedSlot = getSlot(slotId);
                if (clickedSlot instanceof ModularBackpackSlot) {
                    // VOID ANY
                    if (wrapper.canVoid(heldStack, IVoidUpgrade.VoidType.ANY, IVoidUpgrade.VoidInput.ALL)) {
                        inventoryplayer.setItemStack(null);
                        clickedSlot.onSlotChanged();
                        return Platform.EMPTY_STACK;
                    }

                    // VOID OVERFLOW
                    if (wrapper.canVoid(heldStack, IVoidUpgrade.VoidType.OVERFLOW, IVoidUpgrade.VoidInput.ALL)) {
                        wrapper.insertItem(heldStack, false);
                        inventoryplayer.setItemStack(null);
                        clickedSlot.onSlotChanged();
                        return Platform.EMPTY_STACK;
                    }
                }
            }
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
                        if (slot instanceof BackpackCraftingSlot) continue;

                        ItemStack slotStack = slot.getStack();

                        if (func_94527_a(slot, cursor, true) && slot.canTakeStack(player)
                            && func_94530_a(cursor, slot)
                            && (pass != 0 || slotStack.stackSize != slotStack.getMaxStackSize())) {
                            int take = Math.min(cursor.getMaxStackSize() - cursor.stackSize, slotStack.stackSize);

                            ItemStack removed = slot.decrStackSize(take);
                            cursor.stackSize += take;

                            if (removed.stackSize <= 0) slot.putStack(null);

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

                if (fromSlot == null || !fromSlot.canTakeStack(player)) {
                    return Platform.EMPTY_STACK;
                }

                if (fromSlot instanceof ModularFilterSlot) {
                    fromSlot.putStack(null);
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
                boolean isCraftingSlot = clickedSlot instanceof BackpackCraftingSlot;

                if (clickedSlot != null) {
                    ItemStack slotStack = clickedSlot.getStack();

                    if (isFilterSlot) {
                        if (heldStack != null) {
                            ItemStack putStack = heldStack.copy();
                            putStack.stackSize = 1;
                            clickedSlot.putStack(putStack);
                        }

                        if (heldStack == null) {
                            clickedSlot.putStack(null);
                        }

                        return Platform.EMPTY_STACK;
                    }

                    // If slot is empty & player holds something -> try place
                    if (slotStack == null) {
                        if (heldStack != null && clickedSlot.isItemValid(heldStack)) {
                            if (isCraftingSlot) {
                                return Platform.EMPTY_STACK;
                            }

                            if (isBackpackSlot && wrapper != null
                                && wrapper.canVoid(heldStack, IVoidUpgrade.VoidType.ANY, IVoidUpgrade.VoidInput.ALL)) {

                                if (mouseButton == LEFT_MOUSE) {
                                    inventoryplayer.setItemStack(null);
                                } else {
                                    heldStack.splitStack(1);
                                    if (heldStack.stackSize <= 0) {
                                        inventoryplayer.setItemStack(null);
                                    } else {
                                        inventoryplayer.setItemStack(heldStack);
                                    }
                                }

                                clickedSlot.putStack(null);
                                clickedSlot.onSlotChanged();
                                return Platform.EMPTY_STACK;
                            }

                            if (isBackpackSlot && wrapper != null
                                && wrapper
                                    .canVoid(heldStack, IVoidUpgrade.VoidType.OVERFLOW, IVoidUpgrade.VoidInput.ALL)) {

                                if (mouseButton == LEFT_MOUSE) {
                                    ItemStack remainder = wrapper.insertItem(heldStack, false);
                                    if (remainder == null || remainder.stackSize <= 0) {
                                        inventoryplayer.setItemStack(null);
                                        clickedSlot.onSlotChanged();
                                        return Platform.EMPTY_STACK;
                                    }
                                    heldStack = remainder;
                                    inventoryplayer.setItemStack(heldStack);

                                } else {
                                    ItemStack single = heldStack.splitStack(1);
                                    ItemStack remainder = wrapper.insertItem(single, false);
                                    if (remainder == null || remainder.stackSize <= 0) {
                                        if (heldStack.stackSize <= 0) {
                                            inventoryplayer.setItemStack(null);
                                        } else {
                                            inventoryplayer.setItemStack(heldStack);
                                        }
                                        clickedSlot.onSlotChanged();
                                        return Platform.EMPTY_STACK;
                                    } else {
                                        heldStack.stackSize += remainder.stackSize;
                                        inventoryplayer.setItemStack(heldStack);
                                    }
                                }
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

                            ItemStack taken = slotStack.splitStack(take);
                            inventoryplayer.setItemStack(taken);

                            if (slotStack.stackSize == 0) {
                                clickedSlot.putStack(null);
                            } else {
                                clickedSlot.putStack(slotStack);
                            }

                            clickedSlot.onPickupFromSlot(player, inventoryplayer.getItemStack());
                        }
                        // Player holding something
                        else {
                            if (isCraftingSlot) {
                                return Platform.EMPTY_STACK;
                            }

                            // If same item type and tags -> try merge into slot
                            if (slotStack.getItem() == heldStack.getItem()
                                && slotStack.getItemDamage() == heldStack.getItemDamage()
                                && ItemStack.areItemStackTagsEqual(slotStack, heldStack)) {

                                int lim = stackLimit(clickedSlot, heldStack);
                                int canAdd = Math.max(0, lim - slotStack.stackSize);

                                if (canAdd > 0) {
                                    // Void Overflow
                                    if (isBackpackSlot && wrapper != null
                                        && wrapper.canVoid(
                                            heldStack,
                                            IVoidUpgrade.VoidType.OVERFLOW,
                                            IVoidUpgrade.VoidInput.ALL)) {

                                        int add = Math.min(heldStack.stackSize, canAdd);

                                        slotStack.stackSize += add;
                                        clickedSlot.putStack(slotStack);

                                        int overflow = heldStack.stackSize - add;
                                        if (overflow > 0) {
                                            heldStack.stackSize = 0;
                                            inventoryplayer.setItemStack(null);
                                        } else {
                                            heldStack.splitStack(add);
                                            if (heldStack.stackSize <= 0) {
                                                inventoryplayer.setItemStack(null);
                                            }
                                        }
                                        clickedSlot.onSlotChanged();
                                        return null;
                                    }

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
                                    if (isBackpackSlot && wrapper
                                        .canVoid(heldStack, IVoidUpgrade.VoidType.ANY, IVoidUpgrade.VoidInput.ALL)) {
                                        heldStack = null;
                                    }
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
                                            clickedSlot.putStack(null);
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
        if (fromStack == null || fromStack.stackSize <= 0) return fromStack;

        @Nullable
        SlotGroup fromSlotGroup = fromSlot.getSlotGroup();

        // Crafting upgrade slots
        int craftingSlotIndex = parseCraftingSlotIndex(fromSlot.getSlotGroupName());
        if (craftingSlotIndex >= 0) {
            Map<Integer, ICraftingUpgrade> map = wrapper.gatherCapabilityUpgrades(ICraftingUpgrade.class);
            ICraftingUpgrade wrapper = map.get(craftingSlotIndex);

            if (wrapper != null) {
                String targetGroup = switch (wrapper.getCraftingDes()) {
                    case BACKPACK -> "backpack_inventory";
                    case INVENTORY -> "player_inventory";
                };

                for (ModularSlot toSlot : getShiftClickSlots()) {
                    if (!targetGroup.equals(toSlot.getSlotGroupName())) continue;
                    if (!toSlot.isItemValid(fromStack)) continue;
                    transferToSlot(fromSlot, toSlot, fromStack, toSlot.getStack());
                    if (fromStack.stackSize < 1) {
                        return fromStack;
                    }
                }
            }
        }

        // Memorized slots
        if ("player_inventory".equals(fromSlot.getSlotGroupName())) {
            List<ModularSlot> memorizedSlots = getShiftClickSlots().stream()
                .filter(s -> s instanceof ModularBackpackSlot sb && wrapper.isSlotMemorized(sb.getSlotIndex()))
                .collect(Collectors.toList());

            for (ModularSlot toSlot : memorizedSlots) {
                if (!toSlot.isItemValid(fromStack)) continue;
                transferToSlot(fromSlot, toSlot, fromStack, toSlot.getStack());
                if (fromStack.stackSize < 1) {
                    return fromStack;
                }
            }
        }

        for (ModularSlot toSlot : getShiftClickSlots()) {
            SlotGroup slotGroup = Objects.requireNonNull(toSlot.getSlotGroup());
            if (slotGroup != fromSlotGroup && toSlot.func_111238_b() && toSlot.isItemValid(fromStack)) {
                transferToSlot(fromSlot, toSlot, fromStack, toSlot.getStack());
                if (fromStack.stackSize < 1) {
                    return fromStack;
                }
            }
        }

        return super.transferItem(fromSlot, fromStack);
    }

    protected void transferToSlot(ModularSlot fromSlot, ModularSlot toSlot, ItemStack fromStack, ItemStack toStack) {
        boolean isBackpackSlot = toSlot instanceof ModularBackpackSlot;
        if (isBackpackSlot && wrapper.canVoid(fromStack, IVoidUpgrade.VoidType.ANY, IVoidUpgrade.VoidInput.ALL)) {
            fromStack.stackSize = 0;
            toSlot.onSlotChanged();
        }

        int limit = stackLimit(toSlot, fromStack);
        if (fromStack.stackSize > 0 && !fromSlot.isPhantom()
            && ItemHandlerHelper.canItemStacksStack(fromStack, toStack)) {
            int j = toStack.stackSize + fromStack.stackSize;

            if (j <= limit) {
                fromStack.stackSize = 0;
                toStack.stackSize = j;
            } else {
                if (isBackpackSlot
                    && wrapper.canVoid(fromStack, IVoidUpgrade.VoidType.OVERFLOW, IVoidUpgrade.VoidInput.ALL)) {
                    fromStack.stackSize = 0;
                } else {
                    fromStack.stackSize -= limit - toStack.stackSize;
                }
                toStack.stackSize = limit;
            }
            toSlot.onSlotChanged();
        }

        if (fromStack.stackSize > 0 && toStack == null) {
            int move = Math.min(fromStack.stackSize, limit);
            toSlot.putStack(fromStack.splitStack(move));
            toSlot.onSlotChanged();
        }

    }

    protected Integer parseCraftingSlotIndex(String groupName) {
        if (groupName == null) return -1;

        int slotIndexPos = groupName.lastIndexOf("_slot_");
        if (slotIndexPos < 0) return -1;

        try {
            return Integer.parseInt(groupName.substring(slotIndexPos + "_slot_".length()));
        } catch (NumberFormatException e) {
            return -1;
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
}
