package ruiseki.okstoragecore.client.gui.container;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
import ruiseki.okstoragecore.upgrades.IOverflowResponseUpgrade;
import ruiseki.okstoragecore.upgrades.ISlotClickUpgrade;
import ruiseki.okstoragecore.upgrades.IUpgradeItem;

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

    protected final StoragePanel storagePanel;

    public StorageContainer(StoragePanel storagePanel) {
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
        return this.getStoragePanel()
            .getWrapper();
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

        if (isOverflowLogicSlotAndAction(slotId, clickTypeIn)
            && handleOverflow(slotId, clickTypeIn, mouseButton, player)) {
            return ItemHelpers.EMPTY;
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
                            int stackCount = mouseButton == LEFT_MOUSE ? ItemHelpers.getCount(heldStack) : 1;

                            // Limit the stack size that can be placed in the slot
                            int lim = stackLimit(clickedSlot, heldStack);
                            if (stackCount > lim) {
                                stackCount = lim;
                            }

                            // Split the stack from the player's hand and put into the slot
                            ItemStack placed = heldStack.splitStack(stackCount);
                            updateSlotStack(clickedSlot, placed);

                            // If the player runs out of items in hand, set heldStack to null
                            if (ItemHelpers.getCount(heldStack) <= 0) {
                                playerInventory.setItemStack(ItemHelpers.EMPTY);
                            }
                        }
                    } else if (clickedSlot.canTakeStack(player)) { // Slot has items and can be taken
                        if (ItemHelpers.isEmpty(heldStack)) { // Player is not holding any item
                            int s = Math.min(ItemHelpers.getCount(slotStack), slotStack.getMaxStackSize());
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

                                int stackCount = mouseButton == LEFT_MOUSE ? ItemHelpers.getCount(heldStack) : 1;

                                // Limit the number of items that can be merged into the slot
                                int lim = stackLimit(clickedSlot, heldStack);
                                if (stackCount > lim - ItemHelpers.getCount(slotStack)) {
                                    stackCount = lim - ItemHelpers.getCount(slotStack);
                                }

                                // Split items from player's hand
                                if (stackCount > 0) {
                                    ItemHelpers.shrink(heldStack, stackCount);

                                    if (ItemHelpers.getCount(heldStack) <= 0) {
                                        playerInventory.setItemStack(ItemHelpers.EMPTY);
                                    }

                                    ItemStack merged = ItemHelpers.copy(slotStack);
                                    ItemHelpers.grow(merged, stackCount);

                                    updateSlotStack(clickedSlot, merged);
                                }
                            } else if (ItemHelpers.getCount(heldStack) <= stackLimit(clickedSlot, heldStack)) {
                                updateSlotStack(clickedSlot, heldStack);
                                playerInventory.setItemStack(slotStack);
                            }
                        } else if (slotStack.getItem() == heldStack.getItem() && heldStack.getMaxStackSize() > 1
                            && (!slotStack.getHasSubtypes() || slotStack.getItemDamage() == heldStack.getItemDamage())
                            && ItemStack.areItemStackTagsEqual(slotStack, heldStack)) {

                                int stackCount = ItemHelpers.getCount(slotStack);

                                // Merge slot stack into held stack if possible
                                if (stackCount > 0
                                    && stackCount + ItemHelpers.getCount(heldStack) <= heldStack.getMaxStackSize()) {
                                    ItemHelpers.grow(heldStack, stackCount);
                                    slotStack = clickedSlot.decrStackSize(stackCount);

                                    if (ItemHelpers.getCount(slotStack) == 0) {
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

    private boolean handleOverflow(int slotId, ClickType clickType, int dragType, EntityPlayer player) {
        ItemStack cursorStack = clickType == ClickType.SWAP ? player.inventory.getStackInSlot(dragType)
            : player.inventory.getItemStack();

        Consumer<ItemStack> updateCursorStack = clickType == ClickType.SWAP
            ? stack -> player.inventory.setInventorySlotContents(dragType, stack)
            : player.inventory::setItemStack;

        Slot slot = getSlot(slotId);
        if (slot == null || (clickType != ClickType.SWAP && ItemHelpers.isEmpty(cursorStack))
            || !slot.isItemValid(cursorStack)) {
            return false;
        }
        ItemStack slotStack = slot.getStack();
        if (ItemHelpers.isEmpty(slotStack) || (slot.canTakeStack(player) && slotStack.getItem() != cursorStack.getItem()
            && ItemHelpers.getCount(cursorStack) <= stackLimit(slot, cursorStack)
            && ItemHelpers.getCount(slotStack) <= slotStack.getMaxStackSize())) {
            return processOverflowIfSlotWithSameItemFound(slotId, cursorStack, updateCursorStack);
        } else if (slotStack.getItem() == cursorStack.getItem()) {
            return processOverflowForAnythingOverSlotMaxSize(cursorStack, updateCursorStack, slot, slotStack);
        }
        return false;
    }

    private boolean processOverflowForAnythingOverSlotMaxSize(ItemStack cursorStack,
        Consumer<ItemStack> updateCursorStack, Slot slot, ItemStack slotStack) {
        int remainingSpaceInSlot = stackLimit(slot, cursorStack) - ItemHelpers.getCount(slotStack);
        if (remainingSpaceInSlot < ItemHelpers.getCount(cursorStack)) {
            ItemStack overflow = ItemHelpers.copy(cursorStack);
            int overflowCount = ItemHelpers.getCount(cursorStack) - remainingSpaceInSlot;
            ItemHelpers.setCount(overflow, overflowCount);
            ItemStack result = processOverflowLogic(overflow);
            if (ItemHelpers.getCount(result) < overflowCount) {
                ItemHelpers.shrink(cursorStack, overflowCount - ItemHelpers.getCount(result));
                if (ItemHelpers.isEmpty(cursorStack)) {
                    updateCursorStack.accept(ItemHelpers.EMPTY);
                    return true;
                } else {
                    updateCursorStack.accept(cursorStack);
                }
            }
        }
        return false;
    }

    private boolean processOverflowIfSlotWithSameItemFound(int slotId, ItemStack cursorStack,
        Consumer<ItemStack> updateCursorStack) {
        for (IOverflowResponseUpgrade overflowUpgrade : this.getStorageWrapper()
            .getUpgradeHandler()
            .getWrappersThatImplementFromMainStorage(IOverflowResponseUpgrade.class)) {
            if (overflowUpgrade.stackMatchesFilter(cursorStack) && overflowUpgrade.worksInGui()
                && findSlotWithMatchingStack(slotId, cursorStack, updateCursorStack, overflowUpgrade)) {
                return true;
            }
        }
        return false;
    }

    private boolean findSlotWithMatchingStack(int slotId, ItemStack cursorStack, Consumer<ItemStack> updateCursorStack,
        IOverflowResponseUpgrade overflowUpgrade) {
        for (int slotIndex = 0; slotIndex < this.getStorageWrapper()
            .getInventoryHandler()
            .getSlots(); slotIndex++) {
            if (slotIndex != slotId
                && overflowUpgrade.stackMatchesFilterStack(getSlot(slotIndex).getStack(), cursorStack)) {
                ItemStack result = cursorStack;
                result = overflowUpgrade.onOverflow(result);
                updateCursorStack.accept(result);
                if (ItemHelpers.isEmpty(result)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected ItemStack processOverflowLogic(ItemStack stack) {
        ItemStack result = stack;
        for (IOverflowResponseUpgrade overflowUpgrade : this.getStorageWrapper()
            .getUpgradeHandler()
            .getWrappersThatImplementFromMainStorage(IOverflowResponseUpgrade.class)) {
            if (overflowUpgrade.worksInGui()) {
                result = overflowUpgrade.onOverflow(result);
                if (ItemHelpers.isEmpty(result)) {
                    break;
                }
            }
        }
        return result;
    }

    private boolean isOverflowLogicSlotAndAction(int slotId, ClickType clickType) {
        return isStorageInventorySlot(slotId) && (clickType == ClickType.SWAP || clickType == ClickType.PICKUP);
    }

    public boolean isStorageInventorySlot(int index) {
        return index >= 0 && this.inventorySlots.get(index) instanceof ModularStorageSlot;
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

        int originalSize = ItemHelpers.getCount(fromStack);
        if (fromSlot instanceof IndexedModularSlot indexedSlot) {
            boolean activated = indexedSlot.getUpgradeIndex() == this.getStoragePanel()
                .getWrapper()
                .getOpenTabId()
                .orElse(-1);
            if (activated && indexedSlot.getUpgradeWrapper() instanceof ISlotClickUpgrade slotClickUpgrade
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

        if (ItemHelpers.getCount(fromStack) != originalSize) {
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

        for (Predicate<ModularSlot> slotFilter : slotFilters) {
            if (ItemHelpers.isEmpty(fromStack)) {
                return;
            }

            List<ModularSlot> targets = this.inventorySlots.stream()
                .map(slot -> (ModularSlot) slot)
                .filter(slot -> slot.getSlotGroup() != fromSlotGroup)
                .filter(slot -> {
                    if (slot instanceof IndexedModularSlot indexedSlot) {
                        return indexedSlot.getUpgradeIndex() == this.getStoragePanel()
                            .getWrapper()
                            .getOpenTabId()
                            .orElse(-1);
                    }
                    return true;
                })
                .filter(slot -> slot.isItemValid(fromStack))
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

        if (limit <= 0) return;

        if (isStorageInventorySlot(toSlot.slotNumber)) {
            int storageSlotIndex = toSlot.getSlotIndex();
            Consumer<ItemStack> updateSourceStack = result -> ItemHelpers
                .setCount(fromStack, ItemHelpers.getCount(result));
            if (ItemHelpers.isEmpty(toStack)) {
                // Find another storage slot containing a matching item
                if (processOverflowIfSlotWithSameItemFound(storageSlotIndex, fromStack, updateSourceStack)) {
                    updateSlotStack(fromSlot, ItemHelpers.EMPTY);
                }
            } else if (ItemHandlerHelpers.canItemStacksStack(fromStack, toStack)) {
                // Handle anything exceeding the target slot limit
                if (processOverflowForAnythingOverSlotMaxSize(fromStack, updateSourceStack, toSlot, toStack)) {
                    updateSlotStack(fromSlot, ItemHelpers.EMPTY);
                }
            }
        }

        if (!fromSlot.isPhantom() && !ItemHelpers.isEmpty(toStack)
            && ItemHandlerHelpers.canItemStacksStack(fromStack, toStack)) {

            int space = limit - ItemHelpers.getCount(toStack);
            if (space <= 0) return;

            int move = Math.min(space, ItemHelpers.getCount(fromStack));

            ItemStack merged = ItemHelpers.copy(toStack);
            ItemHelpers.grow(merged, move);
            ItemHelpers.shrink(fromStack, move);

            updateSlotStack(toSlot, merged);
        } else if (ItemHelpers.isEmpty(toStack)) {

            int move = Math.min(ItemHelpers.getCount(fromStack), limit);
            if (move <= 0) return;

            ItemStack inserted = fromStack.splitStack(move);

            updateSlotStack(toSlot, inserted);
        }

        if (ItemHelpers.getCount(fromStack) <= 0) {
            ItemHelpers.setCount(fromStack, 0);
            updateSlotStack(fromSlot, ItemHelpers.EMPTY);
        }
    }
}
