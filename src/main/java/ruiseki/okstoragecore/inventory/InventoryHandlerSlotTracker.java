package ruiseki.okstoragecore.inventory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;

import com.gtnewhorizon.gtnhlib.util.data.ItemMeta;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.inventory.ItemStackKey;
import ruiseki.okstoragecore.OKStorageCore;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;

public class InventoryHandlerSlotTracker implements ISlotTracker {

    private final Map<ItemStackKey, Set<Integer>> fullStackSlots = new HashMap<>();
    private final Map<Integer, ItemStackKey> fullSlotStacks = new HashMap<>();
    private final Map<ItemStackKey, Set<Integer>> partiallyFilledStackSlots = new HashMap<>();
    private final Map<Integer, ItemStackKey> partiallyFilledSlotStacks = new HashMap<>();
    private final Map<Item, Set<ItemStackKey>> itemStackKeys = new HashMap<>();
    private final Set<Integer> emptySlots = new TreeSet<>();
    private final MemorySettingsCategory memorySettings;
    private Map<ItemMeta, Set<Integer>> filterItemSlots;
    private Consumer<ItemStackKey> onAddStackKey = sk -> {};
    private Consumer<ItemStackKey> onRemoveStackKey = sk -> {};

    private Runnable onAddFirstEmptySlot = () -> {};
    private Runnable onRemoveLastEmptySlot = () -> {};

    private BooleanSupplier shouldInsertIntoEmpty = () -> true;

    public InventoryHandlerSlotTracker(MemorySettingsCategory memorySettings,
        Map<ItemMeta, Set<Integer>> filterItemSlots) {
        this.memorySettings = memorySettings;
        this.filterItemSlots = filterItemSlots;
    }

    @Override
    public void setShouldInsertIntoEmpty(BooleanSupplier shouldInsertIntoEmpty) {
        this.shouldInsertIntoEmpty = shouldInsertIntoEmpty;
    }

    public void addPartiallyFilled(int slot, ItemStack stack) {
        ItemStackKey stackKey = ItemStackKey.of(stack);
        this.partiallyFilledStackSlots.computeIfAbsent(stackKey, k -> {
            if (!this.fullStackSlots.containsKey(k)) {
                this.onAddStackKey.accept(k);
            }
            return new TreeSet<>();
        })
            .add(slot);
        this.partiallyFilledSlotStacks.put(slot, stackKey);
        this.itemStackKeys.computeIfAbsent(ItemHelpers.getItem(stack), i -> new HashSet<>())
            .add(stackKey);
    }

    @Override
    public Set<ItemStackKey> getFullStacks() {
        return this.fullStackSlots.keySet();
    }

    @Override
    public Set<ItemStackKey> getPartialStacks() {
        return this.partiallyFilledStackSlots.keySet();
    }

    @Override
    public Set<Item> getItems() {
        return this.itemStackKeys.keySet();
    }

    public void addFull(int slot, ItemStack stack) {
        ItemStackKey stackKey = ItemStackKey.of(stack);
        this.fullStackSlots.computeIfAbsent(stackKey, k -> {
            if (!this.partiallyFilledStackSlots.containsKey(k)) {
                this.onAddStackKey.accept(k);
            }
            return new HashSet<>();
        })
            .add(slot);
        this.fullSlotStacks.put(slot, stackKey);
        this.itemStackKeys.computeIfAbsent(ItemHelpers.getItem(stack), i -> new HashSet<>())
            .add(stackKey);
    }

    public void removePartiallyFilled(int slot) {
        if (partiallyFilledSlotStacks.containsKey(slot)) {
            ItemStackKey stackKey = partiallyFilledSlotStacks.remove(slot);
            @Nullable
            Set<Integer> partialSlots = partiallyFilledStackSlots.get(stackKey);
            if (partialSlots == null) {
                OKStorageCore.okLog(
                    Level.ERROR,
                    "Unstable ItemStack detected in slot tracking: {}",
                    stackKey != null ? stackKey.getStack()
                        .toString() : "null");
            } else {
                partialSlots.remove(slot);
            }
            if (partialSlots == null || partialSlots.isEmpty()) {
                partiallyFilledStackSlots.remove(stackKey);
                if (!fullStackSlots.containsKey(stackKey)) {
                    onStackKeyRemoved(stackKey);
                }
            }
        }
    }

    private void removeFull(int slot) {
        if (this.fullSlotStacks.containsKey(slot)) {
            ItemStackKey stackKey = this.fullSlotStacks.remove(slot);
            @Nullable
            Set<Integer> fullSlots = this.fullStackSlots.get(stackKey);
            if (fullSlots == null) {
                OKStorageCore.okLog(
                    Level.ERROR,
                    "Unstable ItemStack detected in slot tracking: {}",
                    stackKey != null ? stackKey.getStack()
                        .toString() : "null");
            } else {
                fullSlots.remove(slot);
            }
            if (fullSlots == null || fullSlots.isEmpty()) {
                this.fullStackSlots.remove(stackKey);
                if (!this.partiallyFilledStackSlots.containsKey(stackKey)) {
                    this.onStackKeyRemoved(stackKey);
                }
            }
        }
    }

    private void onStackKeyRemoved(ItemStackKey stackKey) {
        this.itemStackKeys.computeIfPresent(ItemHelpers.getItem(stackKey.getStack()), (i, stackKeys) -> {
            stackKeys.remove(stackKey);
            return stackKeys;
        });
        if (this.itemStackKeys.containsKey(ItemHelpers.getItem(stackKey.getStack()))
            && this.itemStackKeys.get(ItemHelpers.getItem(stackKey.getStack()))
                .isEmpty()) {
            this.itemStackKeys.remove(ItemHelpers.getItem(stackKey.getStack()));
        }

        this.onRemoveStackKey.accept(stackKey);
    }

    @Override
    public void removeAndSetSlotIndexes(InventoryHandler inventoryHandler, int slot, ItemStack stack) {
        if (ItemHelpers.isEmpty(stack)) {
            this.removePartiallyFilled(slot);
            this.removeFull(slot);
            this.addEmptySlot(slot);
            return;
        }

        if (this.emptySlots.contains(slot)) {
            this.removeEmpty(slot);
        }

        if (this.isPartiallyFilled(inventoryHandler, slot, stack)) {
            this.setPartiallyFilled(slot, stack);
        } else {
            this.setFull(slot, stack);
        }
    }

    private void setFull(int slot, ItemStack stack) {
        boolean containsSlot = this.fullSlotStacks.containsKey(slot);
        if (!containsSlot || this.fullSlotStacks.get(slot)
            .hashCodeNotEquals(stack)) {
            if (containsSlot) {
                this.removeFull(slot);
            }
            this.addFull(slot, stack);
        }
        if (this.partiallyFilledSlotStacks.containsKey(slot)) {
            this.removePartiallyFilled(slot);
        }
    }

    private void setPartiallyFilled(int slot, ItemStack stack) {
        boolean containsSlot = this.partiallyFilledSlotStacks.containsKey(slot);
        if (!containsSlot || this.partiallyFilledSlotStacks.get(slot)
            .hashCodeNotEquals(stack)) {
            if (containsSlot) {
                this.removePartiallyFilled(slot);
            }
            this.addPartiallyFilled(slot, stack);
        }
        if (this.fullSlotStacks.containsKey(slot)) {
            this.removeFull(slot);
        }
    }

    private void removeEmpty(int slot) {
        this.emptySlots.remove(slot);
        if (this.emptySlots.isEmpty()) {
            this.onRemoveLastEmptySlot.run();
        }
    }

    private void set(InventoryHandler inventoryHandler, int slot, ItemStack stack) {
        if (ItemHelpers.isEmpty(stack)) {
            this.addEmptySlot(slot);
        } else {
            if (this.isPartiallyFilled(inventoryHandler, slot, stack)) {
                this.addPartiallyFilled(slot, stack);
            } else {
                this.addFull(slot, stack);
            }
        }
    }

    private void addEmptySlot(int slot) {
        this.emptySlots.add(slot);
        if (this.emptySlots.size() == 1) {
            this.onAddFirstEmptySlot.run();
        }
    }

    @Override
    public void clear() {
        this.partiallyFilledStackSlots.clear();
        this.partiallyFilledSlotStacks.clear();
    }

    @Override
    public void refreshSlotIndexesFrom(InventoryHandler itemHandler) {
        this.fullStackSlots.keySet()
            .forEach(sk -> this.onRemoveStackKey.accept(sk));
        this.fullStackSlots.clear();
        this.fullSlotStacks.clear();
        this.partiallyFilledStackSlots.keySet()
            .forEach(sk -> this.onRemoveStackKey.accept(sk));
        this.partiallyFilledStackSlots.clear();
        this.partiallyFilledSlotStacks.clear();
        this.itemStackKeys.clear();

        this.emptySlots.clear();
        this.onRemoveLastEmptySlot.run();

        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stack = itemHandler.getStackInSlot(slot);
            this.set(itemHandler, slot, stack);
        }
    }

    private boolean isPartiallyFilled(InventoryHandler itemHandler, int slot, ItemStack stack) {
        return ItemHelpers.getCount(stack) < itemHandler.getStackLimit(slot, stack);
    }

    @Override
    public ItemStack insertItemIntoHandler(InventoryHandler itemHandler, IItemHandlerInserter inserter,
        UnaryOperator<ItemStack> overflowHandler, ItemStack stack, boolean simulate) {
        if (this.emptySlots.isEmpty() && !this.itemStackKeys.containsKey(ItemHelpers.getItem(stack))) {
            return stack;
        }

        ItemStackKey stackKey = ItemStackKey.of(stack);
        ItemStack remainingStack = this.handleOverflow(overflowHandler, stackKey, stack);
        if (ItemHelpers.isEmpty(remainingStack)) {
            return remainingStack;
        }
        remainingStack = this.insertIntoSlotsThatMatchStack(inserter, remainingStack, simulate, stackKey);
        if (!ItemHelpers.isEmpty(remainingStack)) {
            remainingStack = this.insertIntoEmptySlots(inserter, remainingStack, simulate);
        }
        if (!ItemHelpers.isEmpty(remainingStack)) {
            remainingStack = this.handleOverflow(overflowHandler, stackKey, remainingStack);
        }
        return remainingStack;
    }

    @Override
    public ItemStack insertItemIntoHandler(InventoryHandler itemHandler, IItemHandlerInserter inserter,
        UnaryOperator<ItemStack> overflowHandler, int slot, ItemStack stack, boolean simulate) {
        return this.insertItemIntoHandler(itemHandler, inserter, overflowHandler, stack, simulate);
    }

    @Override
    public void registerListeners(Consumer<ItemStackKey> onAddStackKey, Consumer<ItemStackKey> onRemoveStackKey,
        Runnable onAddFirstEmptySlot, Runnable onRemoveLastEmptySlot) {
        this.onAddStackKey = onAddStackKey;
        this.onRemoveStackKey = onRemoveStackKey;
        this.onAddFirstEmptySlot = onAddFirstEmptySlot;
        this.onRemoveLastEmptySlot = onRemoveLastEmptySlot;
    }

    @Override
    public void unregisterStackKeyListeners() {
        this.onAddStackKey = sk -> {};
        this.onRemoveStackKey = sk -> {};
    }

    @Override
    public boolean hasEmptySlots() {
        return this.shouldInsertIntoEmpty.getAsBoolean() && !this.emptySlots.isEmpty();
    }

    private ItemStack handleOverflow(UnaryOperator<ItemStack> overflowHandler, ItemStackKey stackKey,
        ItemStack remainingStack) {
        if (this.fullStackSlots.containsKey(stackKey) && !this.fullStackSlots.get(stackKey)
            .isEmpty()) {
            remainingStack = overflowHandler.apply(remainingStack);
        }
        return remainingStack;
    }

    private ItemStack insertIntoSlotsThatMatchStack(IItemHandlerInserter inserter, ItemStack stack, boolean simulate,
        ItemStackKey stackKey) {
        ItemStack remainingStack = stack;

        Set<Integer> slots = this.partiallyFilledStackSlots.get(stackKey);
        if (slots == null || slots.isEmpty()) {
            return remainingStack;
        }

        int sizeBefore = slots.size();
        int i = 0;
        // Always taking first element here and iterating while not empty as iterating using iterator would produce CME
        // due to void/compacting reacting to inserts
        // and going into this logic as well and because of that causing collection to be updated outside of first level
        // iterator. The increment is here just
        // in case updating cache fails to prevent infinite loop
        while (this.partiallyFilledStackSlots.get(stackKey) != null && !this.partiallyFilledStackSlots.get(stackKey)
            .isEmpty() && i++ < sizeBefore) {
            int matchingSlot = this.partiallyFilledStackSlots.get(stackKey)
                .iterator()
                .next();
            remainingStack = inserter.insertItem(matchingSlot, remainingStack, simulate);
            if (ItemHelpers.isEmpty(remainingStack)) {
                break;
            }
        }
        return remainingStack;
    }

    private ItemStack insertIntoEmptySlots(IItemHandlerInserter inserter, ItemStack stack, boolean simulate) {
        if (ItemHelpers.isEmpty(stack)) return ItemHelpers.EMPTY;

        ItemStack remainingStack = this.insertIntoEmptyMemorySlots(inserter, simulate, stack);
        if (ItemHelpers.isEmpty(remainingStack)) return ItemHelpers.EMPTY;

        remainingStack = this.insertIntoEmptyFilterSlots(inserter, simulate, remainingStack);
        if (ItemHelpers.isEmpty(remainingStack)) return ItemHelpers.EMPTY;

        if (this.shouldInsertIntoEmpty.getAsBoolean()) {
            int sizeBefore = this.emptySlots.size();
            int i = 0;
            // Always taking first element here and iterating while not empty as iterating using iterator would produce
            // CME due to void/compacting reacting to inserts
            // and going into this logic as well and because of that causing collection to be updated outside of first
            // level iterator. The increment is here just
            // in case updating cache fails to prevent infinite loop
            while (!this.emptySlots.isEmpty() && i++ < sizeBefore) {
                Iterator<Integer> it = this.emptySlots.iterator();
                int slot = it.next();
                while (this.memorySettings.isSlotSelected(slot)) {
                    if (!it.hasNext()) {
                        return remainingStack;
                    }
                    slot = it.next();
                }

                remainingStack = inserter.insertItem(slot, remainingStack, simulate);
                if (ItemHelpers.isEmpty(remainingStack)) {
                    return ItemHelpers.EMPTY;
                }
            }
        }

        return remainingStack;
    }

    private ItemStack insertIntoEmptyFilterSlots(IItemHandlerInserter inserter, boolean simulate,
        ItemStack remainingStack) {
        if (ItemHelpers.isEmpty(remainingStack)) return ItemHelpers.EMPTY;
        ItemMeta item = new ItemMeta(remainingStack);
        if (this.filterItemSlots.containsKey(item)) {
            for (int filterSlot : this.filterItemSlots.get(item)) {
                if (this.emptySlots.contains(filterSlot)) {
                    remainingStack = inserter.insertItem(filterSlot, remainingStack, simulate);
                    if (ItemHelpers.isEmpty(remainingStack)) {
                        break;
                    }
                }
            }
        }
        return remainingStack;
    }

    private ItemStack insertIntoEmptyMemorySlots(IItemHandlerInserter inserter, boolean simulate, ItemStack stack) {
        if (ItemHelpers.isEmpty(stack)) return ItemHelpers.EMPTY;
        ItemStack remainingStack = stack;
        Map<ItemMeta, Set<Integer>> memoryFilterItemSlots = this.memorySettings.getFilterItemSlots();
        ItemMeta item = new ItemMeta(remainingStack);
        if (memoryFilterItemSlots.containsKey(item)) {
            for (int memorySlot : memoryFilterItemSlots.get(item)) {
                if (this.emptySlots.contains(memorySlot)) {
                    remainingStack = inserter.insertItem(memorySlot, remainingStack, simulate);
                    if (ItemHelpers.isEmpty(remainingStack)) {
                        break;
                    }
                }
            }
        }

        Map<Integer, Set<Integer>> memoryFilterStackSlots = this.memorySettings.getFilterStackSlots();
        if (!memoryFilterStackSlots.isEmpty()) {
            int stackHash = ItemStackKey.getHashCode(remainingStack);
            if (memoryFilterStackSlots.containsKey(stackHash)) {
                for (int memorySlot : memoryFilterStackSlots.get(stackHash)) {
                    if (this.emptySlots.contains(memorySlot)) {
                        remainingStack = inserter.insertItem(memorySlot, remainingStack, simulate);
                        if (ItemHelpers.isEmpty(remainingStack)) {
                            break;
                        }
                    }
                }
            }
        }
        return remainingStack;
    }
}
