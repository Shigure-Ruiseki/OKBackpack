package ruiseki.okbackpack.common.item.travelers.chicken;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemChickenUpgrade extends ItemTravelersUpgradeBase<ChickenUpgradeWrapper> {

    public ItemChickenUpgrade() {
        super("chicken_upgrade", "tooltip.backpack.chicken_upgrade");
    }

    @Override
    public ChickenUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new ChickenUpgradeWrapper(stack, storage, consumer);
    }
}
