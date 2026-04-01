package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

public interface IRestockUpgrade {

    boolean canRestock(ItemStack stack);
}
