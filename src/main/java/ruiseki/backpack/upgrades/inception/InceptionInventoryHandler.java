package ruiseki.backpack.upgrades.inception;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.inventory.ItemStackKey;
import ruiseki.okcore.item.capability.wrapper.CombinedInvWrapper;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okcore.item.handler.IItemHandlerSimpleExtractor;
import ruiseki.okcore.item.handler.IItemHandlerSimpleInserter;
import ruiseki.okstoragecore.inventory.ITrackedContentsItemHandler;

public class InceptionInventoryHandler implements ITrackedContentsItemHandler {

    private IItemHandlerModifiable combinedInventories;
    private final ITrackedContentsItemHandler wrappedInventoryHandler;
    private final InventoryOrder inventoryOrder;
    private final SubBackpacksHandler subBackpacksHandler;
    private List<ITrackedContentsItemHandler> handlers;
    private int[] baseIndex;

    public InceptionInventoryHandler(ITrackedContentsItemHandler wrappedInventoryHandler, InventoryOrder inventoryOrder,
        SubBackpacksHandler subBackpacksHandler) {
        this.wrappedInventoryHandler = wrappedInventoryHandler;
        this.inventoryOrder = inventoryOrder;
        this.subBackpacksHandler = subBackpacksHandler;
        subBackpacksHandler.addRefreshListener(sbs -> refreshHandlerDelegate());

        refreshHandlerDelegate();
    }

    private void refreshHandlerDelegate() {
        handlers = new ArrayList<>();
        if (inventoryOrder == InventoryOrder.MAIN_FIRST) {
            handlers.add(wrappedInventoryHandler);
        }
        subBackpacksHandler.getSubBackpacks()
            .forEach(sbp -> handlers.add(sbp.getInventoryForInputOutput()));
        if (inventoryOrder == InventoryOrder.INCEPTED_FIRST) {
            handlers.add(wrappedInventoryHandler);
        }
        combinedInventories = new CombinedInvWrapper(handlers.toArray(new IItemHandlerModifiable[] {}));

        baseIndex = new int[handlers.size()];
        int index = 0;
        for (int i = 0; i < handlers.size(); i++) {
            index += handlers.get(i)
                .getSlots();
            baseIndex[i] = index;
        }
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        combinedInventories.setStackInSlot(slot, stack);
    }

    @Override
    public int getSlots() {
        return combinedInventories.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return combinedInventories.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return combinedInventories.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return combinedInventories.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return combinedInventories.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return combinedInventories.isItemValid(slot, stack);
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        ItemStack remainingStack = stack;
        for (IItemHandlerSimpleInserter handler : handlers) {
            remainingStack = handler.insertItem(remainingStack, simulate);
            if (ItemHelpers.isEmpty(remainingStack)) {
                break;
            }
        }

        return remainingStack;
    }

    @Override
    public ItemStack extractItem(ItemStack stack, boolean simulate) {
        for (IItemHandlerSimpleExtractor handler : handlers) {
            ItemStack extracted = handler.extractItem(stack, simulate);
            if (!ItemHelpers.isEmpty(extracted)) {
                return extracted;
            }
        }
        return ItemHelpers.EMPTY;
    }

    @Override
    public Set<ItemStackKey> getTrackedStacks() {
        Set<ItemStackKey> ret = new HashSet<>();
        handlers.forEach(h -> ret.addAll(h.getTrackedStacks()));
        return ret;
    }

    @Override
    public void registerTrackingListeners(Consumer<ItemStackKey> onAddStackKey, Consumer<ItemStackKey> onRemoveStackKey,
        Runnable onAddFirstEmptySlot, Runnable onRemoveLastEmptySlot) {
        handlers.forEach(
            h -> h.registerTrackingListeners(
                onAddStackKey,
                onRemoveStackKey,
                onAddFirstEmptySlot,
                onRemoveLastEmptySlot));
    }

    @Override
    public void unregisterStackKeyListeners() {
        handlers.forEach(ITrackedContentsItemHandler::unregisterStackKeyListeners);
    }

    @Override
    public boolean hasEmptySlots() {
        return handlers.stream()
            .anyMatch(ITrackedContentsItemHandler::hasEmptySlots);
    }

    private int getIndexForSlot(int slot) {
        if (slot < 0) {
            return -1;
        }

        for (int i = 0; i < baseIndex.length; i++) {
            if (slot - baseIndex[i] < 0) {
                return i;
            }
        }
        return -1;
    }

    private int getSlotFromIndex(int slot, int index) {
        if (index <= 0 || index >= baseIndex.length) {
            return slot;
        }
        return slot - baseIndex[index - 1];
    }

    private ITrackedContentsItemHandler getHandlerFromIndex(int index) {
        if (index < 0 || index >= handlers.size()) {
            return handlers.get(0);
        }
        return handlers.get(index);
    }
}
