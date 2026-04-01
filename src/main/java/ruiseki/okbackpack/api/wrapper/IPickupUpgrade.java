package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

public interface IPickupUpgrade {

    boolean canPickup(ItemStack stack);
}
