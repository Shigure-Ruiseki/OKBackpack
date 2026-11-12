package ruiseki.backpack.modcompat.thaumcraft;

import net.minecraft.item.ItemStack;

public interface IVisChargeTarget {

    Iterable<ItemStack> getVisChargeableStacks();

    default void onVisCharged() {}
}
