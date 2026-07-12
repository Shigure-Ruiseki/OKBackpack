package ruiseki.okbackpack.common.item.travelers.creeper;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemCreeperUpgrade extends ItemTravelersUpgradeBase<CreeperUpgradeWrapper> {

    public ItemCreeperUpgrade() {
        super("tooltip.backpack.creeper_upgrade");
        setTextureName(Reference.PREFIX_MOD + "creeper_upgrade");
    }

    @Override
    public CreeperUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new CreeperUpgradeWrapper(stack, storage, consumer);
    }
}
