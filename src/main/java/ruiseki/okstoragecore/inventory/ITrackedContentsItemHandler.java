package ruiseki.okstoragecore.inventory;

import java.util.Set;
import java.util.function.Consumer;

import ruiseki.okcore.inventory.ItemStackKey;
import ruiseki.okcore.item.handler.IItemHandlerSimpleInserter;

public interface ITrackedContentsItemHandler extends IItemHandlerSimpleInserter {

    Set<ItemStackKey> getTrackedStacks();

    void registerTrackingListeners(Consumer<ItemStackKey> onAddStackKey, Consumer<ItemStackKey> onRemoveStackKey,
        Runnable onAddFirstEmptySlot, Runnable onRemoveLastEmptySlot);

    void unregisterStackKeyListeners();

    boolean hasEmptySlots();

    int getInternalSlotLimit(int slot);
}
