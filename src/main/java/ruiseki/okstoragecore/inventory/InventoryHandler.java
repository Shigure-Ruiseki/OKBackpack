package ruiseki.okstoragecore.inventory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.gtnewhorizon.gtnhlib.util.data.ItemMeta;

import ruiseki.okcore.datastructure.NonNullList;
import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.MathHelpers;
import ruiseki.okcore.inventory.ItemStackKey;
import ruiseki.okcore.item.handler.IItemHandlerSimpleInserter;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.helper.SlotValueMap;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;
import ruiseki.okstoragecore.upgrades.IInsertResponseUpgrade;
import ruiseki.okstoragecore.upgrades.IOverflowResponseUpgrade;
import ruiseki.okstoragecore.upgrades.voiding.ItemVoidUpgrade;

public abstract class InventoryHandler extends ItemStackHandler
    implements ITrackedContentsItemHandler, IItemHandlerModifiable {

    public static final String INVENTORY_TAG = "inventory";
    private static final String PARTITIONER_TAG = "partitioner";
    protected final IStorageWrapper storageWrapper;
    private final NBTTagCompound contentsNbt;
    private final Runnable saveHandler;
    private final List<IntConsumer> onContentsChangedListeners = new ArrayList<>();
    private final Map<Integer, NBTTagCompound> stackNbts = new LinkedHashMap<>();

    private ISlotTracker slotTracker = new ISlotTracker.Noop();

    private int baseSlotLimit;
    private double maxStackSizeMultiplier;
    private boolean isInitializing;
    // private final StackUpgradeConfig stackUpgradeConfig;
    private final InventoryPartitioner inventoryPartitioner;
    private Consumer<Set<ItemMeta>> filterItemsChangeListener = s -> {};
    private final SlotValueMap<ItemMeta> filterItemSlots = new SlotValueMap<>();
    private BooleanSupplier shouldInsertIntoEmpty = () -> true;
    private IntPredicate isSlotBlocked = slot -> false;
    private IntPredicate shouldRenderBlockedSlotOverlay = slot -> false;
    private boolean voidUpgradeInfoInitialized = false;
    private boolean hasVoidUpgrade = false;

    protected InventoryHandler(int numberOfInventorySlots, IStorageWrapper storageWrapper, NBTTagCompound contentsNbt,
        Runnable saveHandler, int baseSlotLimit) {
        super(numberOfInventorySlots);
        this.isInitializing = true;
        this.storageWrapper = storageWrapper;
        this.contentsNbt = contentsNbt;
        this.saveHandler = saveHandler;
        this.setBaseSlotLimit(baseSlotLimit);
        this.deserializeNBT(contentsNbt.getCompoundTag(INVENTORY_TAG));
        this.inventoryPartitioner = new InventoryPartitioner(
            contentsNbt.getCompoundTag(PARTITIONER_TAG),
            this,
            () -> this.storageWrapper.getSettingsHandler()
                .getTypeCategory(MemorySettingsCategory.class));
        this.initStackNbts();
        this.getSlotTracker()
            .refreshSlotIndexesFrom(this);

        this.isInitializing = false;
    }

    public ISlotTracker getSlotTracker() {
        initSlotTracker();
        return this.slotTracker;
    }

    @Override
    public void setSize(int size) {
        super.setSize(this.stacks.size());
    }

    private void initStackNbts() {
        this.stackNbts.clear();
        for (int slot = 0; slot < this.stacks.size(); slot++) {
            ItemStack slotStack = this.stacks.get(slot);
            if (!ItemHelpers.isEmpty(slotStack)) {
                this.stackNbts.put(slot, this.getSlotsStackNbt(slot, slotStack));
            }
        }
    }

    @Override
    public void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
        if (this.updateSlotNbt(slot)) {
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
        ItemStack slotStack = this.getStackInSlot(slot);
        if (ItemHelpers.isEmpty(slotStack)) {
            if (this.stackNbts.containsKey(slot)) {
                this.stackNbts.remove(slot);
                return true;
            }
        } else {
            NBTTagCompound itemTag = getSlotsStackNbt(slot, slotStack);
            if (!this.stackNbts.containsKey(slot) || !this.stackNbts.get(slot)
                .equals(itemTag)) {
                this.stackNbts.put(slot, itemTag);
                return true;
            }
        }
        return false;
    }

    private NBTTagCompound getSlotsStackNbt(int slot, ItemStack slotStack) {
        NBTTagCompound itemTag = new NBTTagCompound();
        itemTag.setInteger("Slot", slot);
        ItemHelpers.writeToNBT(slotStack, itemTag);
        return itemTag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.slotTracker.clear();
        this.setSize(nbt.hasKey("Size", Constants.NBT.TAG_INT) ? nbt.getInteger("Size") : this.stacks.size());
        NBTTagList tagList = nbt.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < tagList.tagCount(); i++) {
            NBTTagCompound itemTags = tagList.getCompoundTagAt(i);
            int slot = itemTags.getInteger("Slot");
            if (slot >= 0 && slot < this.stacks.size()) {
                ItemStack slotStack = ItemHelpers.readFromNBT(itemTags);
                this.stacks.set(slot, slotStack);
            }
        }
        this.slotTracker.refreshSlotIndexesFrom(this);
        this.onLoad();
    }

    public int getBaseSlotLimit() {
        return baseSlotLimit;
    }

    @Override
    public int getSlotLimit(int slot) {
        return inventoryPartitioner.getPartBySlot(slot)
            .getSlotLimit(slot);
    }

    public int getBaseStackLimit(ItemStack stack) {
        // if (!stackUpgradeConfig.canStackItem(stack.getItem())) {
        // return stack.getMaxStackSize();
        // }
        int maxStackSize = ItemHelpers.isEmpty(stack) ? getBaseSlotLimit() : ItemHelpers.getMaxStackSize(stack);
        if (baseSlotLimit < 64) {
            return (int) Math.max(1, (double) maxStackSize * baseSlotLimit / 64);
        }

        int limit = MathHelpers.intMaxCappedMultiply(maxStackSize, baseSlotLimit / 64);
        int remainder = baseSlotLimit % 64;
        if (remainder > 0) {
            limit = MathHelpers.intMaxCappedAddition(limit, remainder * maxStackSize / 64);
        }
        return limit;
    }

    @Override
    public int getStackLimit(int slot, ItemStack stack) {
        return this.inventoryPartitioner.getPartBySlot(slot)
            .getStackLimit(slot, stack);
    }

    public ItemMeta getFilterItem(int slot) {
        return this.inventoryPartitioner.getPartBySlot(slot)
            .getFilterItem(slot);
    }

    public boolean isFilterItem(ItemMeta item) {
        return this.inventoryPartitioner.isFilterItem(item);
    }

    public void setBaseSlotLimit(int baseSlotLimit) {
        this.voidUpgradeInfoInitialized = false; // not the most ideal of places to do this, but base slot limit is set
                                                 // when
        // upgrades change and that's when slot
        // limit needs to be reinitialized as well
        this.baseSlotLimit = baseSlotLimit;
        this.maxStackSizeMultiplier = baseSlotLimit / 64f;

        if (this.inventoryPartitioner != null) {
            this.inventoryPartitioner.onSlotLimitChange();
        }

        if (!this.isInitializing) {
            this.slotTracker.refreshSlotIndexesFrom(this);
        }
    }

    public ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemHelpers.EMPTY;
        }

        this.validateSlotIndex(slot);
        ItemStack existing = this.getSlotStack(slot);

        if (ItemHelpers.isEmpty(existing)) {
            return ItemHelpers.EMPTY;
        }

        int toExtract = Math.min(amount, ItemHelpers.getMaxStackSize(existing));

        if (ItemHelpers.getCount(existing) <= toExtract) {
            if (!simulate) {
                this.setSlotStack(slot, ItemHelpers.EMPTY);
                return existing;
            } else {
                return ItemHelpers.copy(existing);
            }
        } else {
            if (!simulate) {
                this.setSlotStack(
                    slot,
                    ItemHandlerHelpers.copyStackWithSize(existing, ItemHelpers.getCount(existing) - toExtract));
            }

            return ItemHandlerHelpers.copyStackWithSize(existing, toExtract);
        }
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (isSlotBlocked.test(slot)) return ItemHelpers.EMPTY;
        return inventoryPartitioner.getPartBySlot(slot)
            .extractItem(slot, amount, simulate);
    }

    @Override
    public void validateSlotIndex(int slot) {
        super.validateSlotIndex(slot);
    }

    public ItemStack getSlotStack(int slot) {
        return this.stacks.get(slot);
    }

    public void setSlotStack(int slot, ItemStack stack) {
        this.stacks.set(slot, stack);
        this.getSlotTracker()
            .removeAndSetSlotIndexes(this, slot, stack);
        onContentsChanged(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return this.getSlotTracker()
            .insertItemIntoHandler(
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
        if (!(this.slotTracker instanceof InventoryHandlerSlotTracker)) {
            this.slotTracker = new InventoryHandlerSlotTracker(
                this.storageWrapper.getSettingsHandler()
                    .getTypeCategory(MemorySettingsCategory.class),
                this.filterItemSlots);
            this.slotTracker.refreshSlotIndexesFrom(this);
            this.slotTracker.setShouldInsertIntoEmpty(this.shouldInsertIntoEmpty);
        }
    }

    private ItemStack insertItemInternal(int slot, ItemStack stack, boolean simulate) {
        if (isSlotBlocked.test(slot)) {
            return stack;
        }
        ItemStack ret = runOnBeforeInsert(slot, stack, simulate, this, storageWrapper);
        if (ItemHelpers.isEmpty(ret)) {
            return ret;
        }

        ret = inventoryPartitioner.getPartBySlot(slot)
            .insertItem(slot, ret, simulate, super::insertItem);

        if (!simulate) {
            this.getSlotTracker()
                .removeAndSetSlotIndexes(this, slot, getStackInSlot(slot));
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

    public void runOnAfterInsert(int slot, boolean simulate, IItemHandlerSimpleInserter handler,
        IStorageWrapper storageWrapper) {
        if (!simulate) {
            storageWrapper.getUpgradeHandler()
                .getWrappersThatImplementFromMainStorage(IInsertResponseUpgrade.class)
                .forEach(u -> u.onAfterInsert(handler, slot));
        }
    }

    public ItemStack runOnBeforeInsert(int slot, ItemStack stack, boolean simulate, IItemHandlerSimpleInserter handler,
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
        this.inventoryPartitioner.getPartBySlot(slot)
            .setStackInSlot(slot, stack, super::setStackInSlot);
        this.getSlotTracker()
            .removeAndSetSlotIndexes(this, slot, stack);
    }

    public boolean isItemValid(int slot, ItemStack stack, @Nullable EntityPlayer player) {
        return this.isSlotAvailable(slot) && this.inventoryPartitioner.getPartBySlot(slot)
            .isItemValid(slot, stack, player, super::isItemValid)
            && this.isAllowed(stack)
            && storageWrapper.getSettingsHandler()
                .getTypeCategory(MemorySettingsCategory.class)
                .matchesFilter(slot, stack);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.isItemValid(slot, stack, null);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventoryPartitioner.getPartBySlot(slot)
            .getStackInSlot(slot, super::getStackInSlot);
    }

    protected abstract boolean isAllowed(ItemStack stack);

    public void saveInventory() {
        this.contentsNbt.setTag(INVENTORY_TAG, serializeNBT());
        if (this.inventoryPartitioner != null) {
            // inventory parts may affect inventory slots during their initialization in Inventory Partitioner
            // deserialize,
            // but there's no reason to serialize partitioner at that point as its nbt can't during
            // init/deserialization.
            this.contentsNbt.setTag(PARTITIONER_TAG, inventoryPartitioner.serializeNBT());
        }
        this.saveHandler.run();
    }

    @Nullable
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon(int slotIndex) {
        return inventoryPartitioner.getNoItemIcon(slotIndex);
    }

    public void copyStacksTo(InventoryHandler otherHandler) {
        InventoryHelpers.copyTo(this, otherHandler);
    }

    public void addListener(IntConsumer onContentsChanged) {
        this.onContentsChangedListeners.add(onContentsChanged);
    }

    public void removeListener(IntConsumer onContentsChanged) {
        onContentsChangedListeners.remove(onContentsChanged);
    }

    public void clearListeners() {
        this.onContentsChangedListeners.clear();
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagList nbtTagList = new NBTTagList();
        for (NBTTagCompound tag : this.stackNbts.values()) {
            nbtTagList.appendTag(tag);
        }
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("Items", nbtTagList);
        nbt.setInteger("Size", getSlots());
        return nbt;
    }

    public double getStackSizeMultiplier() {
        return this.maxStackSizeMultiplier;
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
        return this.getSlotTracker()
            .insertItemIntoHandler(this, this::insertItemInternal, this::triggerOverflowUpgrades, stack, simulate);
    }

    @Override
    public ItemStack extractItem(ItemStack stack, boolean simulate) {
        return this.getSlotTracker()
            .extractItemFromHandler(this, this::extractItem, stack, simulate);
    }

    public void changeSlots(int diff) {
        NonNullList<ItemStack> previousStacks = this.stacks;
        this.stacks = NonNullList.withSize(previousStacks.size() + diff, ItemHelpers.EMPTY);
        for (int slot = 0; slot < previousStacks.size() && slot < this.stacks.size(); slot++) {
            this.stacks.set(slot, previousStacks.get(slot));
        }
        this.initStackNbts();
        this.saveInventory();
        this.getSlotTracker()
            .refreshSlotIndexesFrom(this);
    }

    @Override
    public Set<ItemStackKey> getTrackedStacks() {
        this.initSlotTracker();
        HashSet<ItemStackKey> ret = new HashSet<>(this.slotTracker.getFullStacks());
        ret.addAll(this.slotTracker.getPartialStacks());
        return ret;
    }

    @Override
    public void registerTrackingListeners(Consumer<ItemStackKey> onAddStackKey, Consumer<ItemStackKey> onRemoveStackKey,
        Runnable onAddFirstEmptySlot, Runnable onRemoveLastEmptySlot) {
        this.getSlotTracker()
            .registerListeners(onAddStackKey, onRemoveStackKey, onAddFirstEmptySlot, onRemoveLastEmptySlot);
    }

    @Override
    public void unregisterStackKeyListeners() {
        this.slotTracker.unregisterStackKeyListeners();
    }

    @Override
    public boolean hasEmptySlots() {
        return this.slotTracker.hasEmptySlots();
    }

    public InventoryPartitioner getInventoryPartitioner() {
        return this.inventoryPartitioner;
    }

    public boolean isSlotAccessible(int slot) {
        return !this.isSlotBlocked.test(slot) && this.inventoryPartitioner.getPartBySlot(slot)
            .isSlotAccessible(slot);
    }

    public Set<Integer> getNoSortSlots() {
        return this.inventoryPartitioner.getNoSortSlots();
    }

    public void onSlotFilterChanged(int slot) {
        this.inventoryPartitioner.getPartBySlot(slot)
            .onSlotFilterChanged(slot);
    }

    public void registerFilterItemsChangeListener(Consumer<Set<ItemMeta>> listener) {
        this.filterItemsChangeListener = listener;
    }

    public void unregisterFilterItemsChangeListener() {
        this.filterItemsChangeListener = s -> {};
    }

    public void initFilterItems() {
        this.inventoryPartitioner.getFilterItems()
            .forEach((item, slots) -> slots.forEach(slot -> filterItemSlots.add(slot, item)));
    }

    public void onFilterItemsChanged() {
        if (this.inventoryPartitioner == null) {
            return;
        }
        this.filterItemSlots.clear();
        this.inventoryPartitioner.getFilterItems()
            .forEach((item, slots) -> slots.forEach(slot -> filterItemSlots.add(slot, item)));

        this.filterItemsChangeListener.accept(filterItemSlots.keySet());
    }

    public Set<ItemMeta> getFilterItems() {
        return this.filterItemSlots.keySet();
    }

    public void onInit() {
        if (this.inventoryPartitioner == null) {
            return;
        }
        this.inventoryPartitioner.onInit();
        this.slotTracker = new ISlotTracker.Noop();
    }

    public void setShouldInsertIntoEmpty(BooleanSupplier shouldInsertIntoEmpty) {
        this.shouldInsertIntoEmpty = shouldInsertIntoEmpty;
        this.getSlotTracker()
            .setShouldInsertIntoEmpty(shouldInsertIntoEmpty);
    }

    public boolean isInfinite(int slot) {
        return inventoryPartitioner.isInfinite(slot);
    }

    private boolean hasVoidUpgrade() {
        if (!voidUpgradeInfoInitialized) {
            hasVoidUpgrade = !storageWrapper.getUpgradeHandler()
                .getTypeWrappers(ItemVoidUpgrade.TYPE)
                .isEmpty();
            voidUpgradeInfoInitialized = true;
        }
        return hasVoidUpgrade;
    }

    public int getAvailableSlots() {
        int total = getSlots();

        int taken = storageWrapper.getNumberOfSlotRows() * storageWrapper.getColumnsTaken();

        return Math.max(0, Math.min(total, total - taken));
    }

    public boolean isSlotAvailable(int slot) {
        return slot >= 0 && slot < getAvailableSlots();
    }

    @Override
    public List<ItemStack> getStacks() {
        return super.getStacks();
    }

    @Override
    public boolean isSlotFromInventory(int index, IInventory inventory, int invIndex) {
        return super.isSlotFromInventory(index, inventory, invIndex);
    }
}
