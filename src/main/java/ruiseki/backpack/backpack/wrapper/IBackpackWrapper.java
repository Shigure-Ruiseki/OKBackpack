package ruiseki.backpack.backpack.wrapper;

import java.util.UUID;
import java.util.function.IntConsumer;

import net.minecraft.item.ItemStack;

import ruiseki.backpack.backpack.BlockBackpackConfig;
import ruiseki.storagecore.api.IStorageWrapper;

public interface IBackpackWrapper extends IStorageWrapper {

    ItemStack getBackpack();

    void setBackpack(ItemStack stack);

    void setSlotNumbers(int numberOfInventorySlots, int numberOfUpgradeSlots);

    void setContentsUuid(UUID storageUuid);

    default void removeContentsUuid() {
        // noop by default
    }

    default void removeContentsUUIDTag() {
        // noop
    }

    default void registerOnSlotsChangeListener(IntConsumer onSlotsChange) {
        // noop
    }

    default void unregisterOnSlotsChangeListener() {
        // noop
    }

    default void registerOnInventoryHandlerRefreshListener(Runnable onInventoryHandlerRefresh) {
        // noop
    }

    default void unregisterOnInventoryHandlerRefreshListener() {
        // noop
    }

    class Noop extends NoopStorageWrapper implements IBackpackWrapper {

        public static final Noop INSTANCE = new Noop();

        private final ItemStack backpack = new ItemStack(BlockBackpackConfig._instance.getItemInstance());

        @Override
        public ItemStack getBackpack() {
            return backpack;
        }

        @Override
        public void setBackpack(ItemStack stack) {

        }

        @Override
        public void setSlotNumbers(int numberOfInventorySlots, int numberOfUpgradeSlots) {
            // noop
        }

        @Override
        public void setContentsUuid(UUID storageUuid) {
            // noop
        }
    }
}
