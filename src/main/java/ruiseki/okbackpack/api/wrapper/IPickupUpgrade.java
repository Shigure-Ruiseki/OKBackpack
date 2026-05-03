package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

public interface IPickupUpgrade {

    String PICKUP_FILTER_TYPE_TAG = "PickupFilterType";

    boolean canPickup(ItemStack stack);
}
