package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

public interface IPickupUpgrade {

    String PICKUP_FILTER_TYPE_TAG = "PickupFilterType";

    boolean canPickup(ItemStack stack);

    PickupFilterType getPickupFilterType();

    void setPickupFilterType(PickupFilterType type);

    enum PickupFilterType {

        ALLOW,
        BLOCK,
        STORAGE;

        private static final PickupFilterType[] VALUES = values();

        public PickupFilterType next() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }
    }
}
