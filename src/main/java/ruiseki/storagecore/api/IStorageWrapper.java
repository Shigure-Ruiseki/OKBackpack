package ruiseki.storagecore.api;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.inventory.SortBy;
import ruiseki.storagecore.inventory.InventoryHandler;

public interface IStorageWrapper {

    void setSaveHandler(Runnable saveHandler);

    default void setInventorySlotChangeHandler(Runnable slotChangeHandler) {
        // noop
    }

    InventoryHandler getInventoryHandler();

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

    void setPersistent(boolean persistent);

    default void onInit() {
        getInventoryHandler().onInit();
    }

    String getDisplayName();

    public static class NoopStorageWrapper implements IStorageWrapper {

        public static final NoopStorageWrapper INSTANCE = new NoopStorageWrapper();

        @Nullable
        private InventoryHandler inventoryHandler;

        protected NoopStorageWrapper() {

        }

        @Override
        public void setSaveHandler(Runnable saveHandler) {
            // noop
        }

        @Override
        public InventoryHandler getInventoryHandler() {
            if (inventoryHandler == null) {
                inventoryHandler = new InventoryHandler(0, this, new NBTTagCompound(), () -> {}) {

                    @Override
                    public boolean isItemValid(int slot, ItemStack stack) {
                        return true;
                    }
                };
            }
            return inventoryHandler;
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
        public void setPersistent(boolean persistent) {

        }

        @Override
        public String getDisplayName() {
            return "";
        }

    }
}
