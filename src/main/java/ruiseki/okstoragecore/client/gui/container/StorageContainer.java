package ruiseki.okstoragecore.client.gui.container;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.bogosorter.api.IPosSetter;
import com.cleanroommc.bogosorter.api.ISortableContainer;
import com.cleanroommc.bogosorter.api.ISortingContextBuilder;
import com.cleanroommc.modularui.api.inventory.ClickType;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.NEAAnimationHandler;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import cpw.mods.fml.common.Optional;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.slot.ModularStorageSlot;
import ruiseki.okstoragecore.client.gui.slot.ModularUpgradeSlot;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;
import ruiseki.okstoragecore.upgrades.ISlotClickUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeItem;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

@Optional.Interface(iface = "com.cleanroommc.bogosorter.api.ISortableContainer", modid = "bogosorter")
public class StorageContainer extends ModularContainer implements ISortableContainer {

    private static final int DROP_TO_WORLD = -999;
    private static final int LEFT_MOUSE = 0;
    private static final int RIGHT_MOUSE = 1;
    private static final int THROW = 4;
    private static final int STORAGE_SLOT_GROUP_PRIORITY = 10;
    private static final int BOGO_BUTTON_SLEEPING_BAG_GAP = 2;
    private static final int SLEEPING_BAG_BUTTON_SIZE = 14;
    private static final int SLEEPING_BAG_RIGHT_OFFSET = 37;

    private static final String PLAYER_INV = "player_inventory";

    protected final IStorageWrapper storageWrapper;
    protected final StoragePanel storagePanel;

    public StorageContainer(EntityPlayer player, IStorageWrapper storageWrapper, StoragePanel storagePanel) {
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
    }

    @Optional.Method(modid = "bogosorter")
    @Override
    public void buildSortingContext(ISortingContextBuilder builder) {
        List<Slot> storageSlots = new ArrayList<>();

        for (Slot slot : inventorySlots) {
            if (slot instanceof ModularStorageSlot) {
                storageSlots.add(slot);
            }
        }

        if (storageSlots.isEmpty()) return;

        int visualColumns = (int) storageSlots.stream()
            .mapToInt(slot -> slot.xDisplayPosition)
            .distinct()
            .count();

        int columns = Math.min(visualColumns, storageSlots.size());
        builder.addSlotGroupOf(storageSlots, columns)
            .priority(STORAGE_SLOT_GROUP_PRIORITY)
            .buttonPosSetter((slotGroup, buttonPos) -> {
                if (slotGroup.getSlots()
                    .isEmpty()) {
                    buttonPos.setEnabled(false);
                    return;
                }

                int topY = storageSlots.stream()
                    .mapToInt(slot -> slot.yDisplayPosition)
                    .min()
                    .orElse(0);
                int topRightX = storageSlots.stream()
                    .filter(slot -> slot.yDisplayPosition == topY)
                    .mapToInt(slot -> slot.xDisplayPosition)
                    .max()
                    .orElse(0);

                buttonPos.setHorizontal();
                buttonPos.setTopRight();

                buttonPos.setPos(topRightX - 32, topY - 14);
            });
    }

    @Optional.Method(modid = "bogosorter")
    @Override
    public @Nullable IPosSetter getPlayerButtonPosSetter() {
        return (slotGroup, buttonPos) -> {
            if (slotGroup.getSlots()
                .isEmpty()) {
                buttonPos.setEnabled(false);
                return;
            }
            buttonPos.setHorizontal();
            buttonPos.setTopRight();
            int rowSize = this.getStorageWrapper()
                .getNumberOfSlotColumns();
            int panelWidth = 20 + rowSize * 18;
            int sleepingBagLeft = panelWidth - SLEEPING_BAG_RIGHT_OFFSET - SLEEPING_BAG_BUTTON_SIZE;
            int topY = slotGroup.getSlots()
                .getFirst()
                .bogo$getY();
            int buttonTop = topY - 14;
            buttonPos.setPos(sleepingBagLeft - BOGO_BUTTON_SLEEPING_BAG_GAP, buttonTop);
        };
    }

    public IStorageWrapper getStorageWrapper() {
        return storageWrapper;
    }

    public StoragePanel getStoragePanel() {
        return storagePanel;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.getStoragePanel()
            .getActiveUpgradeSlotUpdate()
            .ifPresent(
                update -> {
                    update.getUpgradeContainer()
                        .detectAndSendChanges();
                });
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        super.onCraftMatrixChanged(inventory);
        this.getStoragePanel()
            .getActiveUpgradeSlotUpdate()
            .ifPresent(
                update -> {
                    update.getUpgradeContainer()
                        .onCraftMatrixChanged(inventory);
                });
    }

    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        this.getStoragePanel()
            .getActiveUpgradeSlotUpdate()
            .ifPresent(
                update -> {
                    update.getUpgradeContainer()
                        .onContainerClosed(player);
                });
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
                                playerInventory.setItemStack(ItemHelpers.EMPTY);
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
                                clickedSlot.putStack(ItemHelpers.EMPTY);
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
                                        playerInventory.setItemStack(ItemHelpers.EMPTY);
                                    }

                                    ItemStack merged = slotStack.copy();
                                    merged.stackSize += stackCount;

                                    updateSlotStack(clickedSlot, merged);
                                }
                            } else if (heldStack.stackSize <= stackLimit(clickedSlot, heldStack)) {
                                updateSlotStack(clickedSlot, heldStack);
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
                                        clickedSlot.putStack(ItemHelpers.EMPTY);
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

    public static void updateSlotStack(Slot slot, ItemStack stack) {
        slot.putStack(stack);
        slot.onSlotChanged();
    }

    private MemorySettingsCategory getMemorySettings() {
        return this.getStorageWrapper()
            .getSettingsHandler()
            .getTypeCategory(MemorySettingsCategory.class);
    }

    @Override
    protected ItemStack transferItem(ModularSlot fromSlot, ItemStack fromStack) {
        if (ItemHelpers.isEmpty(fromStack)) {
            return fromStack;
        }

        int originalSize = fromStack.stackSize;

        if (fromSlot instanceof IndexedModularSlot indexedSlot) {
            IUpgradeWrapper wrapper = indexedSlot.getUpgradeWrapper();
            if (wrapper instanceof ISlotClickUpgrade slotClickUpgrade
                && slotClickUpgrade.transferItem(this.getPlayer(), fromSlot, fromStack)) {
                return fromStack;
            }
        }

        if (PLAYER_INV.equals(fromSlot.getSlotGroupName())) {
            this.transferItemFilter(
                fromSlot,
                fromStack,
                stack -> stack.getItem() instanceof IUpgradeItem,
                slot -> slot instanceof ModularUpgradeSlot && !PLAYER_INV.equals(slot.getSlotGroupName()));

            this.transferFiltered(
                fromSlot,
                fromStack,
                slot -> !(slot instanceof ModularUpgradeSlot) && !PLAYER_INV.equals(slot.getSlotGroupName())
                    && this.getMemorySettings()
                        .isSlotSelected(slot.getSlotIndex()));

            this.transferFiltered(
                fromSlot,
                fromStack,
                slot -> !(slot instanceof ModularUpgradeSlot) && !PLAYER_INV.equals(slot.getSlotGroupName())
                    && !this.getMemorySettings()
                        .isSlotSelected(slot.getSlotIndex()));
        } else {
            this.transferFiltered(fromSlot, fromStack, slot -> PLAYER_INV.equals(slot.getSlotGroupName()));
        }

        if (fromStack.stackSize != originalSize) {
            return fromStack;
        }

        return super.transferItem(fromSlot, fromStack);
    }

    @SafeVarargs
    protected final void transferFiltered(ModularSlot fromSlot, ItemStack fromStack,
        Predicate<ModularSlot>... slotFilters) {
        transferItemFilter(fromSlot, fromStack, stack -> true, slotFilters);
    }

    @SafeVarargs
    protected final void transferItemFilter(ModularSlot fromSlot, ItemStack fromStack,
        Predicate<ItemStack> fromStackFilter, Predicate<ModularSlot>... slotFilters) {
        if (ItemHelpers.isEmpty(fromStack) || !fromStackFilter.test(fromStack)) {
            return;
        }

        SlotGroup fromSlotGroup = fromSlot.getSlotGroup();

        // Each predicate represents one priority group.
        for (Predicate<ModularSlot> slotFilter : slotFilters) {
            if (ItemHelpers.isEmpty(fromStack)) {
                return;
            }

            List<ModularSlot> targets = this.inventorySlots.stream()
                .map(slot -> (ModularSlot) slot)
                .filter(slot -> slot.getSlotGroup() != fromSlotGroup)
                .filter(slotFilter)
                .toList();

            for (ModularSlot toSlot : targets) {
                if (ItemHelpers.isEmpty(fromStack)) {
                    return;
                }

                this.transferToSlot(fromSlot, toSlot, fromStack);
            }
        }
    }

    protected void transferToSlot(ModularSlot fromSlot, ModularSlot toSlot, ItemStack fromStack) {
        if (ItemHelpers.isEmpty(fromStack) || !toSlot.isItemValid(fromStack)) {
            return;
        }

        ItemStack toStack = toSlot.getStack();
        int limit = stackLimit(toSlot, fromStack);

        // Merge into an existing stack.
        if (!fromSlot.isPhantom() && ItemHandlerHelpers.canItemStacksStack(fromStack, toStack)) {

            int space = limit - toStack.stackSize;
            if (space <= 0) {
                return;
            }

            int move = Math.min(space, fromStack.stackSize);

            ItemStack merged = toStack.copy();
            merged.stackSize += move;
            fromStack.stackSize -= move;

            updateSlotStack(toSlot, merged);
        } else if (ItemHelpers.isEmpty(toStack)) {
            // Insert into an empty slot.
            int move = Math.min(fromStack.stackSize, limit);
            if (move <= 0) {
                return;
            }

            ItemStack inserted = fromStack.splitStack(move);

            toSlot.putStack(inserted);
            toSlot.onSlotChanged();
        }

        // Normalize the source stack after transfer.
        if (fromStack.stackSize <= 0) {
            fromStack.stackSize = 0;
            updateSlotStack(fromSlot, ItemHelpers.EMPTY);
        }
    }

}
