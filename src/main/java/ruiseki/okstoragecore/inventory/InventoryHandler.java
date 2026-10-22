package ruiseki.okstoragecore.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.gtnewhorizon.gtnhlib.util.data.ItemMeta;

import ruiseki.okcore.datastructure.NonNullList;
import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.MathHelpers;
import ruiseki.okcore.inventory.ItemStackKey;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;
import ruiseki.okstoragecore.upgrades.IInsertResponseUpgrade;
import ruiseki.okstoragecore.upgrades.IOverflowResponseUpgrade;
import ruiseki.okstoragecore.upgrades.ISlotLimitUpgrade;

public abstract class InventoryHandler extends ItemStackHandler implements ITrackedContentsItemHandler {

    public static final String INVENTORY_TAG = "inventory";
    private static final String PARTITIONER_TAG = "partitioner";
    private static final String REAL_COUNT_TAG = "realCount";
    protected final IStorageWrapper storageWrapper;
    private final NBTTagCompound contentsNbt;
    private final Runnable saveHandler;
    private final List<IntConsumer> onContentsChangedListeners = new ArrayList<>();
    private final Map<Integer, NBTTagCompound> stackNbts = new LinkedHashMap<>();

    private ISlotTracker slotTracker = new ISlotTracker.Noop();

    private int baseSlotLimit;
    private int slotLimit;
    private double maxStackSizeMultiplier;
    private boolean isInitializing;
    // private final StackUpgradeConfig stackUpgradeConfig;
    private final InventoryPartitioner inventoryPartitioner;
    private Consumer<Set<ItemMeta>> filterItemsChangeListener = s -> {};
    private final Map<ItemMeta, Set<Integer>> filterItemSlots = new HashMap<>();
    private BooleanSupplier shouldInsertIntoEmpty = () -> true;
    private boolean slotLimitInitialized = false;

    protected InventoryHandler(int numberOfInventorySlots, IStorageWrapper storageWrapper, NBTTagCompound contentsNbt,
        Runnable saveHandler, int baseSlotLimit) {
        super(numberOfInventorySlots);
        isInitializing = true;
        this.storageWrapper = storageWrapper;
        this.contentsNbt = contentsNbt;
        this.saveHandler = saveHandler;
        setBaseSlotLimit(baseSlotLimit);
        this.deserializeNBT(contentsNbt.getCompoundTag(INVENTORY_TAG));
        inventoryPartitioner = new InventoryPartitioner(
            contentsNbt.getCompoundTag(PARTITIONER_TAG),
            this,
            () -> storageWrapper.getSettingsHandler()
                .getTypeCategory(MemorySettingsCategory.class));
        this.initStackNbts();

        isInitializing = false;
    }

    public ISlotTracker getSlotTracker() {
        initSlotTracker();
        return slotTracker;
    }

    @Override
    public void setSize(int size) {
        super.setSize(this.stacks.size());
    }

    private void initStackNbts() {
        for (int slot = 0; slot < stacks.size(); slot++) {
            ItemStack slotStack = stacks.get(slot);
            if (!ItemHelpers.isEmpty(slotStack)) {
                stackNbts.put(slot, getSlotsStackNbt(slot, slotStack));
            }
        }
    }

    @Override
    public void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (updateSlotNbt(slot)) {
            this.saveInventory();
            this.triggerOnChangeListeners(slot);
        }
    }

    public void triggerOnChangeListeners(int slot) {
        for (IntConsumer onContentsChangedListener : this.onContentsChangedListeners) {
            onContentsChangedListener.accept(slot);
        }
    }

    private boolean updateSlotNbt(int slot) {
        ItemStack slotStack = getStackInSlot(slot);
        if (ItemHelpers.isEmpty(slotStack)) {
            if (stackNbts.containsKey(slot)) {
                stackNbts.remove(slot);
                return true;
            }
        } else {
            NBTTagCompound itemTag = getSlotsStackNbt(slot, slotStack);
            if (!stackNbts.containsKey(slot) || !stackNbts.get(slot)
                .equals(itemTag)) {
                stackNbts.put(slot, itemTag);
                return true;
            }
        }
        return false;
    }

    private NBTTagCompound getSlotsStackNbt(int slot, ItemStack slotStack) {
        NBTTagCompound itemTag = new NBTTagCompound();
        itemTag.setInteger("Slot", slot);
        itemTag.setInteger(REAL_COUNT_TAG, slotStack.stackSize);
        slotStack.writeToNBT(itemTag);
        return itemTag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        slotTracker.clear();
        setSize(nbt.hasKey("Size", Constants.NBT.TAG_INT) ? nbt.getInteger("Size") : stacks.size());
        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            int slot = itemTags.getInteger("Slot");

            if (slot >= 0 && slot < stacks.size()) {
                ItemStack slotStack = ItemStack.loadItemStackFromNBT(itemTags);
                if (itemTags.hasKey(REAL_COUNT_TAG)) {
                    ItemHelpers.setCount(slotStack, itemTags.getInteger(REAL_COUNT_TAG));
                }
                stacks.set(slot, slotStack);
            }
        }
        slotTracker.refreshSlotIndexesFrom(this);
        onLoad();
    }

    public int getBaseSlotLimit() {
        return baseSlotLimit;
    }

    @Override
    public int getInternalSlotLimit(int slot) {
        return inventoryPartitioner.getPartBySlot(slot)
            .getSlotLimit(slot);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (!slotLimitInitialized) {
            slotLimitInitialized = true;
            updateSlotLimit();
            inventoryPartitioner.onSlotLimitChange();
        }

        return slotLimit > baseSlotLimit ? slotLimit
            : inventoryPartitioner.getPartBySlot(slot)
                .getSlotLimit(slot);
    }

    public int getBaseStackLimit(ItemStack stack) {
        // if (!stackUpgradeConfig.canStackItem(stack.getItem())) {
        // return stack.getMaxStackSize();
        // }

        int limit = MathHelpers.intMaxCappedMultiply(stack.getMaxStackSize(), (baseSlotLimit / 64));
        int remainder = baseSlotLimit % 64;
        if (remainder > 0) {
            limit = MathHelpers.intMaxCappedAddition(limit, remainder * stack.getMaxStackSize() / 64);
        }
        return limit;
    }

    @Override
    public int getStackLimit(int slot, ItemStack stack) {
        return inventoryPartitioner.getPartBySlot(slot)
            .getStackLimit(slot, stack);
    }

    public ItemMeta getFilterItem(int slot) {
        return inventoryPartitioner.getPartBySlot(slot)
            .getFilterItem(slot);
    }

    public boolean isFilterItem(ItemMeta item) {
        return inventoryPartitioner.isFilterItem(item);
    }

    public void setBaseSlotLimit(int baseSlotLimit) {
        slotLimitInitialized = false; // not the most ideal of places to do this, but base slot limit is set when
                                      // upgrades change and that's when slot limit needs to be reinitialized as well
        this.baseSlotLimit = baseSlotLimit;
        maxStackSizeMultiplier = baseSlotLimit / 64f;

        if (inventoryPartitioner != null) {
            inventoryPartitioner.onSlotLimitChange();
        }

        if (!isInitializing) {
            slotTracker.refreshSlotIndexesFrom(this);
        }
    }

    private void updateSlotLimit() {
        AtomicInteger slotLimitOverride = new AtomicInteger(baseSlotLimit);
        storageWrapper.getUpgradeHandler()
            .getWrappersThatImplement(ISlotLimitUpgrade.class)
            .forEach(slu -> {
                if (slu.getSlotLimit() > slotLimitOverride.get()) {
                    slotLimitOverride.set(slu.getSlotLimit());
                }
            });
        slotLimit = slotLimitOverride.get();
    }

    public ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemHelpers.EMPTY;
        }

        validateSlotIndex(slot);
        ItemStack existing = getSlotStack(slot);

        if (ItemHelpers.isEmpty(existing)) {
            return ItemHelpers.EMPTY;
        }

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (ItemHelpers.getCount(existing) <= toExtract) {
            if (!simulate) {
                setSlotStack(slot, ItemHelpers.EMPTY);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                setSlotStack(
                    slot,
                    ItemHandlerHelpers.copyStackWithSize(existing, ItemHelpers.getCount(existing) - toExtract));
            }

            return ItemHandlerHelpers.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return inventoryPartitioner.getPartBySlot(slot)
            .extractItem(slot, amount, simulate);
    }

    @Override
    public void validateSlotIndex(int slot) {
        super.validateSlotIndex(slot);
    }

    public ItemStack getSlotStack(int slot) {
        return stacks.get(slot);
    }

    public void setSlotStack(int slot, ItemStack stack) {
        stacks.set(slot, stack);
        slotTracker.removeAndSetSlotIndexes(this, slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        initSlotTracker();
        return slotTracker.insertItemIntoHandler(
            this,
            this::insertItemInternal,
            this::triggerOverflowUpgrades,
            slot,
            stack,
            simulate);
    }

    public ItemStack insertItemOnlyToSlot(int slot, ItemStack stack, boolean simulate) {
        initSlotTracker();
        if (ItemHandlerHelpers.canItemStacksStack(getStackInSlot(slot), stack)) {
            return triggerOverflowUpgrades(insertItemInternal(slot, stack, simulate));
        }

        return insertItemInternal(slot, stack, simulate);
    }

    private void initSlotTracker() {
        if (!(slotTracker instanceof InventoryHandlerSlotTracker)) {
            slotTracker = new InventoryHandlerSlotTracker(
                storageWrapper.getSettingsHandler()
                    .getTypeCategory(MemorySettingsCategory.class),
                filterItemSlots);
            slotTracker.refreshSlotIndexesFrom(this);
            slotTracker.setShouldInsertIntoEmpty(shouldInsertIntoEmpty);
        }
    }

    private ItemStack insertItemInternal(int slot, ItemStack stack, boolean simulate) {
        ItemStack ret = runOnBeforeInsert(slot, stack, simulate, this, storageWrapper);
        if (ItemHelpers.isEmpty(ret)) {
            return ret;
        }

        ret = inventoryPartitioner.getPartBySlot(slot)
            .insertItem(slot, ret, simulate, super::insertItem);

        if (!simulate) {
            slotTracker.removeAndSetSlotIndexes(this, slot, getStackInSlot(slot));
        }

        if (ret == stack) {
            return ret;
        }

        runOnAfterInsert(slot, simulate, this, storageWrapper);

        return ret;
    }

    private ItemStack triggerOverflowUpgrades(ItemStack ret) {
        for (IOverflowResponseUpgrade overflowUpgrade : storageWrapper.getUpgradeHandler()
            .getWrappersThatImplement(IOverflowResponseUpgrade.class)) {
            ret = overflowUpgrade.onOverflow(ret);
            if (ItemHelpers.isEmpty(ret)) {
                break;
            }
        }
        return ret;
    }

    private void runOnAfterInsert(int slot, boolean simulate, IItemHandlerSimpleInserter handler,
        IStorageWrapper storageWrapper) {
        if (!simulate) {
            storageWrapper.getUpgradeHandler()
                .getWrappersThatImplementFromMainStorage(IInsertResponseUpgrade.class)
                .forEach(u -> u.onAfterInsert(handler, slot));
        }
    }

    private ItemStack runOnBeforeInsert(int slot, ItemStack stack, boolean simulate, IItemHandlerSimpleInserter handler,
        IStorageWrapper storageWrapper) {
        List<IInsertResponseUpgrade> wrappers = storageWrapper.getUpgradeHandler()
            .getWrappersThatImplementFromMainStorage(IInsertResponseUpgrade.class);
        ItemStack remaining = stack;
        for (IInsertResponseUpgrade upgrade : wrappers) {
            remaining = upgrade.onBeforeInsert(handler, slot, remaining, simulate);
            if (ItemHelpers.isEmpty(remaining)) {
                return ItemHelpers.EMPTY;
            }
        }
        return remaining;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        inventoryPartitioner.getPartBySlot(slot)
            .setStackInSlot(slot, stack, super::setStackInSlot);
        slotTracker.removeAndSetSlotIndexes(this, slot, stack);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return inventoryPartitioner.getPartBySlot(slot)
            .isItemValid(slot, stack) && isAllowed(stack)
            && storageWrapper.getSettingsHandler()
                .getTypeCategory(MemorySettingsCategory.class)
                .matchesFilter(slot, stack);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventoryPartitioner.getPartBySlot(slot)
            .getStackInSlot(slot, super::getStackInSlot);
    }

    protected abstract boolean isAllowed(ItemStack stack);

    public void saveInventory() {
        contentsNbt.setTag(INVENTORY_TAG, serializeNBT());
        if (inventoryPartitioner != null) {
            // inventory parts may affect inventory slots during their initialization in Inventory Partitioner
            // deserialize,
            // but there's no reason to serialize partitioner at that point as its nbt can't during
            // init/deserialization.
            contentsNbt.setTag(PARTITIONER_TAG, inventoryPartitioner.serializeNBT());
        }
        saveHandler.run();
    }

    @Nullable
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon(int slotIndex) {
        return inventoryPartitioner.getNoItemIcon(slotIndex);
    }

    public void copyStacksTo(InventoryHandler otherHandler) {
        InventoryHelpers.copyTo(this, otherHandler);
    }

    public void addListener(IntConsumer onContentsChanged) {
        onContentsChangedListeners.add(onContentsChanged);
    }

    public void clearListeners() {
        onContentsChangedListeners.clear();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagList nbtTagList = new NBTTagList();
        for (NBTTagCompound tag : stackNbts.values()) {
            nbtTagList.appendTag(tag);
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Items", nbtTagList);
        nbt.setInteger("Size", getSlots());
        return nbt;
    }

    public double getStackSizeMultiplier() {
        return maxStackSizeMultiplier;
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        initSlotTracker();
        return slotTracker
            .insertItemIntoHandler(this, this::insertItemInternal, this::triggerOverflowUpgrades, stack, simulate);
    }

    public void changeSlots(int diff) {
        List<ItemStack> previousStacks = stacks;
        stacks = NonNullList.withSize(previousStacks.size() + diff, ItemHelpers.EMPTY);
        for (int slot = 0; slot < previousStacks.size() && slot < stacks.size(); slot++) {
            stacks.set(slot, previousStacks.get(slot));
        }
        initStackNbts();
        saveInventory();
        slotTracker.refreshSlotIndexesFrom(this);
    }

    @Override
    public Set<ItemStackKey> getTrackedStacks() {
        initSlotTracker();
        HashSet<ItemStackKey> ret = new HashSet<>(slotTracker.getFullStacks());
        ret.addAll(slotTracker.getPartialStacks());
        return ret;
    }

    @Override
    public void registerTrackingListeners(Consumer<ItemStackKey> onAddStackKey, Consumer<ItemStackKey> onRemoveStackKey,
        Runnable onAddFirstEmptySlot, Runnable onRemoveLastEmptySlot) {
        initSlotTracker();
        slotTracker.registerListeners(onAddStackKey, onRemoveStackKey, onAddFirstEmptySlot, onRemoveLastEmptySlot);
    }

    @Override
    public void unregisterStackKeyListeners() {
        slotTracker.unregisterStackKeyListeners();
    }

    @Override
    public boolean hasEmptySlots() {
        return slotTracker.hasEmptySlots();
    }

    public InventoryPartitioner getInventoryPartitioner() {
        return inventoryPartitioner;
    }

    public boolean isSlotAccessible(int slot) {
        return inventoryPartitioner.getPartBySlot(slot)
            .isSlotAccessible(slot);
    }

    public Set<Integer> getNoSortSlots() {
        return inventoryPartitioner.getNoSortSlots();
    }

    public void onSlotFilterChanged(int slot) {
        inventoryPartitioner.getPartBySlot(slot)
            .onSlotFilterChanged(slot);
    }

    public void registerFilterItemsChangeListener(Consumer<Set<ItemMeta>> listener) {
        filterItemsChangeListener = listener;
    }

    public void unregisterFilterItemsChangeListener() {
        filterItemsChangeListener = s -> {};
    }

    public void initFilterItems() {
        filterItemSlots.putAll(inventoryPartitioner.getFilterItems());
    }

    public void onFilterItemsChanged() {
        if (inventoryPartitioner == null) {
            return;
        }
        filterItemSlots.clear();
        filterItemSlots.putAll(inventoryPartitioner.getFilterItems());

        filterItemsChangeListener.accept(filterItemSlots.keySet());
    }

    public Set<ItemMeta> getFilterItems() {
        return filterItemSlots.keySet();
    }

    public void onInit() {
        if (inventoryPartitioner == null) {
            return;
        }
        inventoryPartitioner.onInit();
        slotTracker = new ISlotTracker.Noop();
    }

    public void setShouldInsertIntoEmpty(BooleanSupplier shouldInsertIntoEmpty) {
        this.shouldInsertIntoEmpty = shouldInsertIntoEmpty;
        slotTracker.setShouldInsertIntoEmpty(shouldInsertIntoEmpty);
    }
}
