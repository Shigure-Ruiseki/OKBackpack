package ruiseki.backpack.backpack.wrapper;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.backpack.BackpackStorage;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.inventory.InventorySorter;
import ruiseki.okcore.inventory.ItemStackKey;
import ruiseki.okcore.inventory.SortBy;
import ruiseki.storagecore.inventory.InventoryHandler;

public class BackpackWrapper implements IBackpackWrapper {

    public static final int DEFAULT_CLOTH_COLOR = 13394234;
    public static final int DEFAULT_BORDER_COLOR = 6434330;
    private static final String CLOTH_COLOR_TAG = "clothColor";
    private static final String BORDER_COLOR_TAG = "borderColor";
    private static final String OPEN_TAB_ID_TAG = "openTabId";
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

    private Runnable onInventoryHandlerRefresh = () -> {};

    public boolean isDirty;

    public BackpackWrapper(@Nullable ItemStack backpack, String tierId) {
        this.backpack = backpack;
        this.tierId = tierId;
    }

    @Override
    public InventoryHandler getInventoryHandler() {
        if (handler == null) {
            handler = new InventoryHandler(54, this, getBackpackContentsNbt(), () -> {
                this.markBackpackContentsDirty();
                this.inventorySlotChangeHandler.run();
            });
        }
        return handler;
    }

    @Override
    public @Nullable ItemStack getBackpack() {
        return this.backpack;
    }

    @Override
    public void setBackpack(@Nullable ItemStack stack) {
        this.backpack = stack;
    }

    @Override
    public void setContentsUuid(UUID storageUuid) {
        NBTHelpers.setUniqueId(backpack, CONTENTS_UUID_TAG, storageUuid);
    }

    @Override
    public void setSaveHandler(Runnable saveHandler) {
        backpackSaveHandler = saveHandler;
    }

    @Override
    public void setInventorySlotChangeHandler(Runnable slotChangeHandler) {
        inventorySlotChangeHandler = slotChangeHandler;
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
        // if (upgradeHandler == Noop.INSTANCE.getUpgradeHandler()) {
        // upgradeHandler = null;
        // }
        // if (settingsHandler == Noop.INSTANCE.getSettingsHandler()) {
        // settingsHandler = null;
        // }
    }

    private NBTTagCompound getBackpackContentsNbt() {
        return BackpackStorage.get()
            .getOrCreateBackpackContents(getOrCreateContentsUuid());
    }

    private void markBackpackContentsDirty() {
        BackpackStorage.get()
            .markDirty();
    }

    private void migrateBackpackContents(UUID newUuid) {
        migrateNbtTag(newUuid, InventoryHandler.INVENTORY_TAG);

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
    public void setSortBy(SortBy sortBy) {
        NBTHelpers.setString(backpack, SORT_BY_TAG, sortBy.getName());
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
    public void onContentsNbtUpdated() {
        handler = null;
        // upgradeHandler = null;
        // refreshInventoryForUpgradeProcessing();
        onInventoryHandlerRefresh.run();
    }

    @Override
    public void setPersistent(boolean persistent) {
        getInventoryHandler().setPersistent(persistent);
        // getUpgradeHandler().setPersistent(persistent);
    }

    @Override
    public void setSlotNumbers(int numberOfInventorySlots, int numberOfUpgradeSlots) {
        setNumberOfInventorySlots(numberOfInventorySlots);
        // setNumberOfUpgradeSlots(numberOfUpgradeSlots);
    }

    private void setNumberOfInventorySlots(int itemInventorySlots) {
        NBTHelpers.setInteger(backpack, INVENTORY_SLOTS_TAG, itemInventorySlots);
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
    public String getDisplayName() {
        return getTierInventoryName();
    }

    public String getTierInventoryName() {
        if (tierId == null) return null;
        return TierRegistry.getDisplayName(tierId);
    }
}
