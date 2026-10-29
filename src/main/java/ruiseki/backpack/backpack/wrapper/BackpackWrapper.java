package ruiseki.backpack.backpack.wrapper;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntConsumer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.backpack.BackpackStorage;
import ruiseki.backpack.backpack.ItemBackpack;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okcore.helper.Helpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.inventory.InventorySorter;
import ruiseki.okcore.inventory.ItemStackKey;
import ruiseki.okcore.inventory.SortBy;
import ruiseki.okstoragecore.inventory.ITrackedContentsItemHandler;
import ruiseki.okstoragecore.inventory.InventoryHandler;
import ruiseki.okstoragecore.inventory.InventoryIOHandler;
import ruiseki.okstoragecore.settings.SettingsHandler;
import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;
import ruiseki.okstoragecore.settings.nosort.NoSortSettingsCategory;
import ruiseki.okstoragecore.upgrades.UpgradeHandler;
import ruiseki.okstoragecore.upgrades.stack.ItemStackUpgrade;

public class BackpackWrapper implements IBackpackWrapper {

    public static final int DEFAULT_CLOTH_COLOR = 13394234;
    public static final int DEFAULT_BORDER_COLOR = 6434330;
    private static final String CLOTH_COLOR_TAG = "clothColor";
    private static final String BORDER_COLOR_TAG = "borderColor";
    private static final String OPEN_TAB_ID_TAG = "openTabId";
    private static final String TAB_INDEX_TAG = "tabIndex";
    private static final String SORT_BY_TAG = "sortBy";
    private static final String CONTENTS_UUID_TAG = "contentsUuid";
    private static final String INVENTORY_SLOTS_TAG = "inventorySlots";
    private static final String UPGRADE_SLOTS_TAG = "upgradeSlots";
    private static final String COLUMNS_TAKEN_TAG = "columnsTaken";

    @Nullable
    private ItemStack backpack;
    private String tierId;
    private Runnable backpackSaveHandler = () -> {};
    private Runnable inventorySlotChangeHandler = () -> {};

    @Nullable
    private InventoryHandler handler;
    @Nullable
    private UpgradeHandler upgradeHandler = null;
    @Nullable
    private InventoryIOHandler inventoryIOHandler = null;
    @Nullable
    private InventoryModificationHandler inventoryModificationHandler = null;
    @Nullable
    private SettingsHandler settingsHandler = null;

    private IntConsumer onSlotsChange = diff -> {};

    private Runnable onInventoryHandlerRefresh = () -> {};
    private Runnable upgradeCachesInvalidatedHandler = () -> {};

    public BackpackWrapper(@Nullable ItemStack backpack, String tierId) {
        this.backpack = backpack;
        this.tierId = tierId;
    }

    @Override
    public void setSaveHandler(Runnable saveHandler) {
        backpackSaveHandler = saveHandler;
        refreshInventoryForUpgradeProcessing();
    }

    @Override
    public void setInventorySlotChangeHandler(Runnable slotChangeHandler) {
        inventorySlotChangeHandler = slotChangeHandler;
    }

    @Override
    public ITrackedContentsItemHandler getInventoryForUpgradeProcessing() {
        if (inventoryModificationHandler == null) {
            inventoryModificationHandler = new InventoryModificationHandler(this);
        }
        return inventoryModificationHandler.getModifiedInventoryHandler();
    }

    @Override
    public InventoryHandler getInventoryHandler() {
        if (handler == null) {
            handler = new BackpackInventoryHandler(getNumberOfInventorySlots(), this, getBackpackContentsNbt(), () -> {
                this.markBackpackContentsDirty();
                this.inventorySlotChangeHandler.run();
            }, ItemStackUpgrade.getInventorySlotLimit(this));
            // handler.addListener(getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class)::itemChanged);
        }
        return handler;
    }

    private int getNumberOfInventorySlots() {
        Optional<Integer> inventorySlots = NBTHelpers.getInteger(backpack, INVENTORY_SLOTS_TAG);

        if (inventorySlots.isPresent()) {
            return inventorySlots.get();
        }

        int itemInventorySlots = ((ItemBackpack) backpack.getItem()).getNumberOfSlots();
        setNumberOfInventorySlots(itemInventorySlots);
        return itemInventorySlots;
    }

    @Override
    public boolean useWideLayout() {
        return getNumberOfInventorySlots() > 81;
    }

    @Override
    public int getNumberOfSlotRows() {
        return (int) Math.ceil((double) getNumberOfInventorySlots() / (useWideLayout() ? 12 : 9));
    }

    @Override
    public int getNumberOfSlotColumns() {
        return useWideLayout() ? 12 : 9;
    }

    private void setNumberOfInventorySlots(int itemInventorySlots) {
        NBTHelpers.setInteger(backpack, INVENTORY_SLOTS_TAG, itemInventorySlots);
    }

    private NBTTagCompound getBackpackContentsNbt() {
        return BackpackStorage.get()
            .getOrCreateBackpackContents(getOrCreateContentsUuid());
    }

    private void markBackpackContentsDirty() {
        BackpackStorage.get()
            .markDirty();
    }

    @Override
    public ITrackedContentsItemHandler getInventoryForInputOutput() {
        if (inventoryIOHandler == null) {
            inventoryIOHandler = new InventoryIOHandler(this);
        }
        return inventoryIOHandler.getFilteredItemHandler();
    }

    @Override
    public SettingsHandler getSettingsHandler() {
        if (settingsHandler == null) {
            if (getContentsUuid().isPresent()) {
                settingsHandler = new BackpackSettingsHandler(
                    this,
                    getBackpackContentsNbt(),
                    this::markBackpackContentsDirty);
            } else {
                settingsHandler = NoopBackpackWrapper.INSTANCE.getSettingsHandler();
            }
        }
        return settingsHandler;
    }

    @Override
    public UpgradeHandler getUpgradeHandler() {
        if (upgradeHandler == null) {
            if (getContentsUuid().isPresent()) {
                upgradeHandler = new UpgradeHandler(
                    getNumberOfUpgradeSlots(),
                    this,
                    getBackpackContentsNbt(),
                    this::markBackpackContentsDirty,
                    () -> {
                        if (handler != null) {
                            handler.clearListeners();
                            handler.setBaseSlotLimit(ItemStackUpgrade.getInventorySlotLimit(this));
                        }
                        getInventoryHandler().clearListeners();
                        // handler.addListener(getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class)::itemChanged);
                        inventoryIOHandler = null;
                        inventoryModificationHandler = null;
                        // fluidHandlerInitialized = false;
                        // fluidHandler = null;
                        // energyStorageInitialized = false;
                        // energyStorage = null;
                        upgradeCachesInvalidatedHandler.run();
                    }) {

                    @Override
                    public boolean isItemValid(int slot, ItemStack stack) {
                        // noinspection ConstantConditions - by this time the upgrade has registryName so it can't be
                        // null
                        return super.isItemValid(slot, stack)
                            && (ItemHelpers.isEmpty(stack) || OKBackpack._instance.getModId()
                                .equals(
                                    Objects.requireNonNull(Helpers.getLocation(stack))
                                        .getResourceDomain()));
                    }
                };
            } else {
                upgradeHandler = NoopBackpackWrapper.INSTANCE.getUpgradeHandler();
            }
        }
        return upgradeHandler;
    }

    @Override
    public void setUpgradeCachesInvalidatedHandler(Runnable handler) {
        upgradeCachesInvalidatedHandler = handler;
    }

    private int getNumberOfUpgradeSlots() {
        Optional<Integer> upgradeSlots = NBTHelpers.getInteger(backpack, UPGRADE_SLOTS_TAG);

        if (upgradeSlots.isPresent()) {
            return upgradeSlots.get();
        }

        int itemUpgradeSlots = ((ItemBackpack) backpack.getItem()).getNumberOfUpgradeSlots();
        setNumberOfUpgradeSlots(itemUpgradeSlots);
        return itemUpgradeSlots;
    }

    @Override
    public Optional<UUID> getContentsUuid() {
        return NBTHelpers.getUniqueId(backpack, CONTENTS_UUID_TAG);
    }

    @Override
    public UUID getOrCreateContentsUuid() {
        Optional<UUID> contentsUuid = getContentsUuid();
        if (contentsUuid.isPresent()) {
            return contentsUuid.get();
        }
        clearDummyHandlers();
        UUID newUuid = UUID.randomUUID();
        setContentsUuid(newUuid);
        migrateBackpackContents(newUuid);
        return newUuid;
    }

    private void clearDummyHandlers() {
        if (upgradeHandler == NoopBackpackWrapper.INSTANCE.getUpgradeHandler()) {
            upgradeHandler = null;
        }
        if (settingsHandler == NoopBackpackWrapper.INSTANCE.getSettingsHandler()) {
            settingsHandler = null;
        }
    }

    private void migrateBackpackContents(UUID newUuid) {
        migrateNbtTag(newUuid, InventoryHandler.INVENTORY_TAG);
        migrateNbtTag(newUuid, UpgradeHandler.UPGRADE_INVENTORY_TAG);
    }

    private void migrateNbtTag(UUID newUuid, String key) {
        NBTHelpers.getCompoundTag(backpack, key)
            .ifPresent(nbt -> {
                BackpackStorage.get()
                    .getOrCreateBackpackContents(newUuid)
                    .setTag(key, nbt);
                markBackpackContentsDirty();
                NBTHelpers.removeTag(backpack, key);
            });
    }

    @Override
    public int getMainColor() {
        return NBTHelpers.getInteger(backpack, CLOTH_COLOR_TAG)
            .orElse(DEFAULT_CLOTH_COLOR);
    }

    @Override
    public int getAccentColor() {
        return NBTHelpers.getInteger(backpack, BORDER_COLOR_TAG)
            .orElse(DEFAULT_BORDER_COLOR);
    }

    @Override
    public void setColors(int mainColor, int accentColor) {
        NBTHelpers.setInteger(backpack, CLOTH_COLOR_TAG, mainColor);
        NBTHelpers.setInteger(backpack, BORDER_COLOR_TAG, accentColor);
        backpackSaveHandler.run();
    }

    @Override
    public Optional<Integer> getOpenTabId() {
        return NBTHelpers.getInteger(backpack, OPEN_TAB_ID_TAG);
    }

    @Override
    public void setOpenTabId(int openTabId) {
        NBTHelpers.setInteger(backpack, OPEN_TAB_ID_TAG, openTabId);
        backpackSaveHandler.run();
    }

    @Override
    public void removeOpenTabId() {
        NBTHelpers.removeTag(backpack, OPEN_TAB_ID_TAG);
        backpackSaveHandler.run();
    }

    @Override
    public Optional<Integer> getTabIndex() {
        return NBTHelpers.getInteger(backpack, TAB_INDEX_TAG);
    }

    @Override
    public void setTabIndex(int tabIndex) {
        NBTHelpers.setInteger(backpack, TAB_INDEX_TAG, tabIndex);
        backpackSaveHandler.run();
    }

    @Override
    public void removeTabIndex() {
        NBTHelpers.removeTag(backpack, TAB_INDEX_TAG);
        backpackSaveHandler.run();
    }

    @Override
    public void setSortBy(SortBy sortBy) {
        NBTHelpers.setString(backpack, SORT_BY_TAG, sortBy.getSerializedName());
        backpackSaveHandler.run();
    }

    @Override
    public SortBy getSortBy() {
        return NBTHelpers.getEnumConstant(backpack, SORT_BY_TAG, SortBy::fromName)
            .orElse(SortBy.NAME);
    }

    @Override
    public void sort() {
        Set<Integer> slotIndexesExcludedFromSort = new HashSet<>();
        slotIndexesExcludedFromSort.addAll(
            getSettingsHandler().getTypeCategory(NoSortSettingsCategory.class)
                .getNoSortSlots());
        slotIndexesExcludedFromSort.addAll(
            getSettingsHandler().getTypeCategory(MemorySettingsCategory.class)
                .getSlotIndexes());
        InventorySorter.sortHandler(getInventoryHandler(), getComparator(), slotIndexesExcludedFromSort);
    }

    private Comparator<Map.Entry<ItemStackKey, Integer>> getComparator() {
        return switch (getSortBy()) {
            case MOD_ID -> InventorySorter.BY_MOD_ID;
            case COUNT -> InventorySorter.BY_COUNT;
            case TAG -> InventorySorter.BY_TAG;
            case NAME -> InventorySorter.BY_NAME;
            case ORE_DICT -> InventorySorter.BY_ORE_DICT;
        };
    }

    @Override
    public @Nullable ItemStack getBackpack() {
        return this.backpack;
    }

    @Override
    public void refreshInventoryForInputOutput() {
        inventoryIOHandler = null;
        upgradeCachesInvalidatedHandler.run();
    }

    @Override
    public void setSlotNumbers(int numberOfInventorySlots, int numberOfUpgradeSlots) {
        setNumberOfInventorySlots(numberOfInventorySlots);
        setNumberOfUpgradeSlots(numberOfUpgradeSlots);
    }

    @Override
    public void setContentsUuid(UUID storageUuid) {
        NBTHelpers.setUniqueId(backpack, CONTENTS_UUID_TAG, storageUuid);
    }

    @Override
    public void removeContentsUuid() {
        getContentsUuid().ifPresent(BackpackStorage.get()::removeBackpackContents);
        removeContentsUUIDTag();
    }

    @Override
    public void removeContentsUUIDTag() {
        NBTHelpers.removeTag(backpack, CONTENTS_UUID_TAG);
    }

    @Override
    public void setColumnsTaken(int columnsTaken, boolean hasChanged) {
        int originalColumnsTaken = getColumnsTaken();
        NBTHelpers.setInteger(backpack, COLUMNS_TAKEN_TAG, columnsTaken);
        if (hasChanged) {
            int diff = (columnsTaken - originalColumnsTaken) * getNumberOfSlotRows();
            onSlotsChange.accept(diff);
        }
        backpackSaveHandler.run();
    }

    @Override
    public int getColumnsTaken() {
        return NBTHelpers.getInteger(backpack, COLUMNS_TAKEN_TAG)
            .orElse(0);
    }

    @Override
    public void onContentsNbtUpdated() {
        handler = null;
        upgradeHandler = null;
        refreshInventoryForUpgradeProcessing();
        onInventoryHandlerRefresh.run();
    }

    @Override
    public void registerOnSlotsChangeListener(IntConsumer onSlotsChange) {
        this.onSlotsChange = onSlotsChange;
    }

    private void setNumberOfUpgradeSlots(int numberOfUpgradeSlots) {
        NBTHelpers.setInteger(backpack, UPGRADE_SLOTS_TAG, numberOfUpgradeSlots);
    }

    @Override
    public void refreshInventoryForUpgradeProcessing() {
        inventoryModificationHandler = null;
        // fluidHandler = null;
        // fluidHandlerInitialized = false;
        // energyStorage = null;
        // energyStorageInitialized = false;
        refreshInventoryForInputOutput();
    }

    @Override
    public void unregisterOnSlotsChangeListener() {
        onSlotsChange = diff -> {};
    }

    @Override
    public void registerOnInventoryHandlerRefreshListener(Runnable onInventoryHandlerRefresh) {
        this.onInventoryHandlerRefresh = onInventoryHandlerRefresh;
    }

    @Override
    public void unregisterOnInventoryHandlerRefreshListener() {
        onInventoryHandlerRefresh = () -> {};
    }

    @Override
    public ItemStack getWrappedStorageStack() {
        return this.getBackpack();
    }

    @Override
    public String getStorageType() {
        return "backpack";
    }

    @Override
    public String getDisplayName() {
        return getTierInventoryName();
    }

    public String getTierInventoryName() {
        if (tierId == null) return null;
        return TierRegistry.getDisplayName(tierId);
    }
}
