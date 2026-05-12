package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

/**
 * Restock upgrade marker interface. Transfers items from target container to backpack.
 */
public interface IRestockUpgrade extends IInventoryInteractionUpgrade {

    String RESTOCK_FILTER_TYPE_TAG = "RestockFilterType";

    default boolean canRestock(ItemStack stack) {
        return true;
    }

    RestockFilterType getRestockFilterType();

    void setRestockFilterType(RestockFilterType type);

    enum RestockFilterType {

        ALLOW,
        BLOCK,
        STORAGE;

        private static final RestockFilterType[] VALUES = values();

        public RestockFilterType next() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }
    }
}
