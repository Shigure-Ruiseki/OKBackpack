package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

public interface IRestockUpgrade {

    boolean canRestock(ItemStack stack);
}
