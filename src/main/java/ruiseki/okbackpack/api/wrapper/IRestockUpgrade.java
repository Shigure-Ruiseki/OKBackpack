package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

/**
 * Restock upgrade marker interface. Transfers items from target container to backpack.
 */
public interface IRestockUpgrade extends IInventoryInteractionUpgrade {

    default boolean canRestock(ItemStack stack) {
        return true;
    }
}
