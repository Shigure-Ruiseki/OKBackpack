package ruiseki.okbackpack.common.item.travelers.creeper;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemCreeperUpgrade extends ItemTravelersUpgradeBase<CreeperUpgradeWrapper> {

    public ItemCreeperUpgrade() {
        super("creeper_upgrade", "tooltip.backpack.creeper_upgrade");
    }

    @Override
    public CreeperUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new CreeperUpgradeWrapper(stack, storage, consumer);
    }
}
