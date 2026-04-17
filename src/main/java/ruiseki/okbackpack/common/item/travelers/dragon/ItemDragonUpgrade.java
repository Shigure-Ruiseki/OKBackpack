package ruiseki.okbackpack.common.item.travelers.dragon;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemDragonUpgrade extends ItemTravelersUpgradeBase<DragonUpgradeWrapper> {

    public ItemDragonUpgrade() {
        super("dragon_upgrade", "tooltip.backpack.dragon_upgrade");
    }

    @Override
    public DragonUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new DragonUpgradeWrapper(stack, storage, consumer);
    }
}
