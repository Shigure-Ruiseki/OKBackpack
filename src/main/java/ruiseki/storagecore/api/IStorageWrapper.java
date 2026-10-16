package ruiseki.storagecore.api;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.inventory.SortBy;
import ruiseki.storagecore.inventory.InventoryHandler;
import ruiseki.storagecore.settings.ISettingsCategory;
import ruiseki.storagecore.settings.SettingsHandler;
import ruiseki.storagecore.settings.main.MainSettingsCategory;
import ruiseki.storagecore.upgrades.UpgradeHandler;

public interface IStorageWrapper {

    void setSaveHandler(Runnable saveHandler);

    default void setInventorySlotChangeHandler(Runnable slotChangeHandler) {
        // noop
    }

    InventoryHandler getInventoryHandler();

    default void setUpgradeCachesInvalidatedHandler(Runnable handler) {
        // noop
    }

    SettingsHandler getSettingsHandler();

    UpgradeHandler getUpgradeHandler();

    Optional<UUID> getContentsUuid();

    UUID getOrCreateContentsUuid();

    int getMainColor();

    int getAccentColor();

    void setColors(int mainColor, int accentColor);

    Optional<Integer> getOpenTabId();

    void setOpenTabId(int openTabId);

    void removeOpenTabId();

    void setSortBy(SortBy sortBy);

    SortBy getSortBy();

    void sort();

    void onContentsNbtUpdated();

    void refreshInventoryForUpgradeProcessing();

    void refreshInventoryForInputOutput();

    void setColumnsTaken(int columnsTaken, boolean hasChanged);

    int getColumnsTaken();

    default boolean useWideLayout() {
        return false;
    }

    default int getNumberOfSlotRows() {
        return 0;
    }

    default int getNumberOfSlotColumns() {
        return 0;
    }

    default ItemStack getWrappedStorageStack() {
        return ItemHelpers.EMPTY;
    }

    default int getBaseStackSizeMultiplier() {
        return 1;
    }

    default void onInit() {
        getInventoryHandler().onInit();
    }

    String getStorageType();

    String getDisplayName();

    public static class NoopStorageWrapper implements IStorageWrapper {

        public static final NoopStorageWrapper INSTANCE = new NoopStorageWrapper();

        @Nullable
        private InventoryHandler inventoryHandler;

        @Nullable
        private SettingsHandler settingsHandler;

        @Nullable
        private UpgradeHandler upgradeHandler;

        protected NoopStorageWrapper() {

        }

        @Override
        public void setSaveHandler(Runnable saveHandler) {
            // noop
        }

        @Override
        public InventoryHandler getInventoryHandler() {
            if (inventoryHandler == null) {
                inventoryHandler = new InventoryHandler(0, this, new NBTTagCompound(), () -> {}, 64) {

                    @Override
                    public boolean isItemValid(int slot, ItemStack stack) {
                        return true;
                    }

                    @Override
                    protected boolean isAllowed(ItemStack stack) {
                        return false;
                    }
                };
            }
            return inventoryHandler;
        }

        @Override
        public SettingsHandler getSettingsHandler() {
            if (settingsHandler == null) {
                settingsHandler = new SettingsHandler(new NBTTagCompound(), () -> {}, this::getInventoryHandler) {

                    @Override
                    protected NBTTagCompound getSettingsNbtFromContentsNbt(NBTTagCompound contentsNbt) {
                        return contentsNbt;
                    }

                    @Override
                    protected void addItemDisplayCategory(Supplier<InventoryHandler> inventoryHandlerSupplier,
                        NBTTagCompound settingsNbt) {
                        // noop
                    }

                    @Override
                    public String getGlobalSettingsCategoryName() {
                        return "";
                    }

                    @Override
                    public ISettingsCategory<?> instantiateGlobalSettingsCategory(NBTTagCompound categoryNbt,
                        Consumer<NBTTagCompound> saveNbt) {
                        return new MainSettingsCategory<>(categoryNbt, saveNbt, "");
                    }

                    @Override
                    protected void saveCategoryNbt(NBTTagCompound settingsNbt, String categoryName,
                        NBTTagCompound tag) {
                        // noop
                    }
                };
            }
            return settingsHandler;
        }

        @Override
        public UpgradeHandler getUpgradeHandler() {
            if (upgradeHandler == null) {
                upgradeHandler = new UpgradeHandler(0, this, new NBTTagCompound(), () -> {}, () -> {});
            }
            return upgradeHandler;
        }

        @Override
        public Optional<UUID> getContentsUuid() {
            return Optional.empty();
        }

        @Override
        public UUID getOrCreateContentsUuid() {
            return UUID.randomUUID();
        }

        @Override
        public int getMainColor() {
            return -1;
        }

        @Override
        public int getAccentColor() {
            return -1;
        }

        @Override
        public void setColors(int mainColor, int accentColor) {

        }

        @Override
        public Optional<Integer> getOpenTabId() {
            return Optional.empty();
        }

        @Override
        public void setOpenTabId(int openTabId) {

        }

        @Override
        public void removeOpenTabId() {

        }

        @Override
        public void setSortBy(SortBy sortBy) {

        }

        @Override
        public SortBy getSortBy() {
            return SortBy.NAME;
        }

        @Override
        public void sort() {

        }

        @Override
        public void onContentsNbtUpdated() {

        }

        @Override
        public void refreshInventoryForUpgradeProcessing() {

        }

        @Override
        public void refreshInventoryForInputOutput() {

        }

        @Override
        public void setColumnsTaken(int columnsTaken, boolean hasChanged) {

        }

        @Override
        public int getColumnsTaken() {
            return 0;
        }

        @Override
        public String getStorageType() {
            return "";
        }

        @Override
        public String getDisplayName() {
            return "";
        }

    }
}
