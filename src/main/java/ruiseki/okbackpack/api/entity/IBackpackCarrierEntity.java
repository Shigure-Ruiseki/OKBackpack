package ruiseki.okbackpack.api.entity;

import net.minecraft.item.ItemStack;

public interface IBackpackCarrierEntity {

    Iterable<ItemStack> getCarriedBackpacks();
}
