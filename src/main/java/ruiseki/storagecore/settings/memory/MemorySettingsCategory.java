package ruiseki.storagecore.settings.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.gtnewhorizon.gtnhlib.util.data.ItemMeta;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.inventory.ItemStackKey;
import ruiseki.storagecore.inventory.InventoryHandler;
import ruiseki.storagecore.settings.ISettingsCategory;

public class MemorySettingsCategory implements ISettingsCategory<MemorySettingsCategory> {

    public static final String NAME = "memory";
    private static final String SLOT_FILTER_ITEMS_TAG = "slotFilterItems";
    private static final String SLOT_FILTER_STACKS_TAG = "slotFilterStacks";
    private static final String IGNORE_NBT_TAG = "ignoreNbt";
    private final Supplier<InventoryHandler> inventoryHandlerSupplier;
    private NBTTagCompound categoryNbt;
    private final Consumer<NBTTagCompound> saveNbt;
    private final Map<Integer, ItemMeta> slotFilterItems = new LinkedHashMap<>();
    private final Map<Integer, ItemStackKey> slotFilterStacks = new LinkedHashMap<>();
    private final Map<ItemMeta, Set<Integer>> filterItemSlots = new HashMap<>();
    private final Map<Integer, Set<Integer>> filterStackSlots = new HashMap<>();

    private boolean ignoreNbt = true;
    private Consumer<ItemMeta> onItemAdded = i -> {};
    private Consumer<Integer> onStackAdded = i -> {};
    private Consumer<ItemMeta> onItemRemoved = i -> {};
    private Consumer<Integer> onStackRemoved = i -> {};

    public MemorySettingsCategory(Supplier<InventoryHandler> inventoryHandlerSupplier, NBTTagCompound categoryNbt,
        Consumer<NBTTagCompound> saveNbt) {
        this.inventoryHandlerSupplier = inventoryHandlerSupplier;
        this.categoryNbt = categoryNbt;
        this.saveNbt = saveNbt;

        deserialize();
    }

    private void deserialize() {
        NBTHelpers.getMap(categoryNbt, SLOT_FILTER_ITEMS_TAG, Integer::valueOf, (k, v) -> {
            NBTTagCompound tag = (NBTTagCompound) v;
            Item item = (Item) Item.itemRegistry.getObject(tag.getString("name"));
            int meta = tag.getInteger("meta");
            return Optional.of(new ItemMeta(item, meta));
        })
            .ifPresent(map -> map.forEach(this::addSlotItem));

        NBTHelpers
            .getMap(
                categoryNbt,
                SLOT_FILTER_STACKS_TAG,
                Integer::valueOf,
                (k, v) -> v instanceof NBTTagCompound tag ? Optional.ofNullable(ItemStack.loadItemStackFromNBT(tag))
                    : Optional.empty())
            .ifPresent(map -> map.forEach(this::addSlotStack));
        ignoreNbt = NBTHelpers.getBoolean(categoryNbt, IGNORE_NBT_TAG)
            .orElse(true);
    }

    public boolean matchesFilter(int slotNumber, ItemStack stack) {
        if (slotFilterItems.containsKey(slotNumber)) {
            return !ItemHelpers.isEmpty(stack) && slotFilterItems.get(slotNumber)
                .matches(stack);
        }
        if (slotFilterStacks.containsKey(slotNumber)) {
            return !ItemHelpers.isEmpty(stack) && slotFilterStacks.get(slotNumber)
                .matches(stack);
        }

        return true;
    }

    public Optional<ItemStack> getSlotFilterStack(int slotNumber, boolean copy) {
        if (slotFilterItems.containsKey(slotNumber)) {
            return Optional.of(
                slotFilterItems.get(slotNumber)
                    .toStack(1));
        }
        if (slotFilterStacks.containsKey(slotNumber)) {
            ItemStack filterStack = slotFilterStacks.get(slotNumber)
                .getStack();
            return Optional.of(copy ? filterStack.copy() : filterStack);
        }

        return Optional.empty();
    }

    public boolean isSlotSelected(int slotNumber) {
        return slotFilterItems.containsKey(slotNumber) || slotFilterStacks.containsKey(slotNumber);
    }

    public void unselectAllSlots() {
        unselectAllFilterItemSlots();
        unselectAllFilteStackSlots();

        serializeFilterItems();
    }

    private void unselectAllFilteStackSlots() {
        filterStackSlots.keySet()
            .forEach(i -> onStackRemoved.accept(i));
        slotFilterStacks.clear();
        filterStackSlots.clear();
    }

    private void unselectAllFilterItemSlots() {
        filterItemSlots.keySet()
            .forEach(i -> onItemRemoved.accept(i));
        slotFilterItems.clear();
        filterItemSlots.clear();
    }

    /**
     * Selects slots that shouldn't be sorted
     *
     * @param minSlot inclusive
     * @param maxSlot exclusive
     */
    public void selectSlots(int minSlot, int maxSlot) {
        for (int slot = minSlot; slot < maxSlot; slot++) {
            InventoryHandler inventoryHandler = getInventoryHandler();
            if (slot < inventoryHandler.getSlots()) {
                ItemStack stackInSlot = inventoryHandler.getStackInSlot(slot);
                if (!ItemHelpers.isEmpty(stackInSlot)) {
                    if (ignoreNbt) {
                        addSlotItem(slot, new ItemMeta(stackInSlot));
                    } else {
                        addSlotStack(slot, stackInSlot);
                    }
                } else {
                    ItemMeta filterItem = inventoryHandler.getFilterItem(slot);
                    if (filterItem != null) {
                        if (ignoreNbt) {
                            addSlotItem(slot, filterItem);
                        } else {
                            addSlotStack(slot, filterItem.toStack(1));
                        }
                    }
                }
            }
        }
        serializeFilterItems();
    }

    private InventoryHandler getInventoryHandler() {
        return inventoryHandlerSupplier.get();
    }

    private void addSlotItem(int slot, ItemMeta item) {
        slotFilterItems.put(slot, item);
        filterItemSlots.computeIfAbsent(item, k -> {
            onItemAdded.accept(k);
            return new TreeSet<>();
        })
            .add(slot);
    }

    private void addSlotStack(int slot, ItemStack stack) {
        ItemStackKey isk = ItemStackKey.of(stack);
        slotFilterStacks.put(slot, isk);
        int stackHash = isk.hashCode();
        filterStackSlots.computeIfAbsent(stackHash, k -> {
            onStackAdded.accept(stackHash);
            return new TreeSet<>();
        })
            .add(slot);
    }

    public void selectSlot(int slotNumber) {
        selectSlots(slotNumber, slotNumber + 1);
    }

    public void unselectSlot(int slotNumber) {
        unselectFilterItemSlot(slotNumber);
        unselectFilterStackSlot(slotNumber);
        serializeFilterItems();
    }

    private void unselectFilterItemSlot(int slotNumber) {
        if (!slotFilterItems.containsKey(slotNumber)) {
            return;
        }

        ItemMeta item = slotFilterItems.remove(slotNumber);
        Set<Integer> itemSlots = filterItemSlots.get(item);
        itemSlots.remove(slotNumber);
        if (itemSlots.isEmpty()) {
            filterItemSlots.remove(item);
            onItemRemoved.accept(item);
        }
    }

    private void unselectFilterStackSlot(int slotNumber) {
        if (!slotFilterStacks.containsKey(slotNumber)) {
            return;
        }

        ItemStackKey isk = slotFilterStacks.remove(slotNumber);
        int stackHash = isk.hashCode();
        Set<Integer> stackSlots = filterStackSlots.get(stackHash);
        stackSlots.remove(slotNumber);
        if (stackSlots.isEmpty()) {
            filterStackSlots.remove(stackHash);
            onStackRemoved.accept(stackHash);
        }
    }

    public boolean ignoresNbt() {
        return ignoreNbt;
    }

    public void setIgnoreNbt(boolean ignoreNbt) {
        if (this.ignoreNbt == ignoreNbt) {
            return;
        }

        Set<Integer> slotIndexes = getSlotIndexes();
        if (this.ignoreNbt && !ignoreNbt) {
            slotFilterItems.forEach((slot, item) -> {
                ItemStack stack = inventoryHandlerSupplier.get()
                    .getStackInSlot(slot);
                if (ItemHelpers.isEmpty(stack)) {
                    stack = item.toStack(1);
                }
                addSlotStack(slot, stack);
            });
            unselectAllFilterItemSlots();
        } else {
            slotFilterStacks.forEach((slot, isk) -> addSlotItem(slot, new ItemMeta(isk.getStack())));
            unselectAllFilteStackSlots();
        }
        serializeFilterItems();

        this.ignoreNbt = ignoreNbt;
        serializeIgnoreNbt();
        slotIndexes.forEach(this::selectSlot);
    }

    private void serializeIgnoreNbt() {
        categoryNbt.setBoolean(IGNORE_NBT_TAG, ignoreNbt);
        saveNbt.accept(categoryNbt);
    }

    private void serializeFilterItems() {
        // noinspection ConstantConditions - item registry name exists in this content otherwise player wouldn't be able
        // to work with it
        NBTHelpers.setMap(categoryNbt, SLOT_FILTER_ITEMS_TAG, slotFilterItems, String::valueOf, i -> {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("name", Item.itemRegistry.getNameForObject(i.getItem()));
            tag.setInteger("meta", i.getItemMeta());
            return tag;
        });
        NBTHelpers.setMap(
            categoryNbt,
            SLOT_FILTER_STACKS_TAG,
            slotFilterStacks,
            String::valueOf,
            isk -> isk.getStack()
                .writeToNBT(new NBTTagCompound()));
        saveNbt.accept(categoryNbt);
    }

    @Override
    public void reloadFrom(NBTTagCompound categoryNbt) {
        this.categoryNbt = categoryNbt;
        slotFilterItems.clear();
        filterItemSlots.clear();
        slotFilterStacks.clear();
        filterStackSlots.clear();
        deserialize();
    }

    @Override
    public void overwriteWith(MemorySettingsCategory otherCategory) {
        unselectAllSlots();

        ignoreNbt = otherCategory.ignoreNbt;

        if (ignoreNbt) {
            overwriteFilterItems(otherCategory);
        } else {
            overwriteFilterStacks(otherCategory);
        }
        serializeIgnoreNbt();
        serializeFilterItems();
    }

    private void overwriteFilterStacks(MemorySettingsCategory otherCategory) {
        InventoryHandler inventoryHandler = getInventoryHandler();
        otherCategory.slotFilterStacks.forEach((slot, isk) -> {
            if (slot >= inventoryHandler.getSlots()) {
                return;
            }

            ItemStack stackInSlot = inventoryHandler.getStackInSlot(slot);
            if (ItemHelpers.isEmpty(stackInSlot) || otherCategory.matchesFilter(slot, stackInSlot)) {
                addSlotStack(slot, isk.getStack());
            }
        });
    }

    private void overwriteFilterItems(MemorySettingsCategory otherCategory) {
        InventoryHandler inventoryHandler = getInventoryHandler();
        otherCategory.slotFilterItems.forEach((slot, item) -> {
            if (slot >= inventoryHandler.getSlots()) {
                return;
            }

            ItemStack stackInSlot = inventoryHandler.getStackInSlot(slot);
            if (ItemHelpers.isEmpty(stackInSlot) || otherCategory.matchesFilter(slot, stackInSlot)) {
                addSlotItem(slot, item);
            }
        });
    }

    public Set<Integer> getSlotIndexes() {
        HashSet<Integer> slots = new HashSet<>(slotFilterItems.keySet());
        slots.addAll(slotFilterStacks.keySet());
        return slots;
    }

    public Map<ItemMeta, Set<Integer>> getFilterItemSlots() {
        return filterItemSlots;
    }

    public Map<Integer, Set<Integer>> getFilterStackSlots() {
        return filterStackSlots;
    }

    public boolean matchesFilter(ItemStack stack) {
        return filterItemSlots.containsKey(stack.getItem())
            || (!filterStackSlots.isEmpty() && filterStackSlots.containsKey(ItemStackKey.getHashCode(stack)));
    }

    public void registerListeners(Consumer<ItemMeta> onItemAdded, Consumer<ItemMeta> onItemRemoved,
        Consumer<Integer> onStackAdded, Consumer<Integer> onStackRemoved) {
        this.onItemAdded = onItemAdded;
        this.onItemRemoved = onItemRemoved;
        this.onStackAdded = onStackAdded;
        this.onStackRemoved = onStackRemoved;
    }

    public void unregisterListeners() {
        onItemAdded = i -> {};
        onItemRemoved = i -> {};
        onStackAdded = i -> {};
        onStackRemoved = i -> {};
    }

    public void setFilter(int slot, ItemStack filter) {
        InventoryHandler inventoryHandler = getInventoryHandler();
        if (slot < inventoryHandler.getSlots()) {
            ItemStack stackInSlot = inventoryHandler.getStackInSlot(slot);
            if (ItemHelpers.isEmpty(stackInSlot)) {
                if (ignoreNbt) {
                    addSlotItem(slot, new ItemMeta(filter));
                } else {
                    addSlotStack(slot, filter);
                }
            }
        }
        serializeFilterItems();
    }

    @Override
    public boolean isLargerThanNumberOfSlots(int slots) {
        return slotFilterItems.keySet()
            .stream()
            .anyMatch(slotIndex -> slotIndex >= slots)
            || slotFilterStacks.keySet()
                .stream()
                .anyMatch(slotIndex -> slotIndex >= slots);
    }
}
