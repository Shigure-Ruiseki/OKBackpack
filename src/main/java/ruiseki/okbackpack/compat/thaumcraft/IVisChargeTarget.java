package ruiseki.okbackpack.compat.thaumcraft;

import net.minecraft.item.ItemStack;

public interface IVisChargeTarget {

    Iterable<ItemStack> getVisChargeableStacks();

    default void onVisCharged() {}
}
