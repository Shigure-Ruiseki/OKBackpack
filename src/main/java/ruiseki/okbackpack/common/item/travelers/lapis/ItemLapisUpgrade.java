package ruiseki.okbackpack.common.item.travelers.lapis;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemLapisUpgrade extends ItemTravelersUpgradeBase<LapisUpgradeWrapper> {

    public ItemLapisUpgrade() {
        super("tooltip.backpack.lapis_upgrade");
        setTextureName(Reference.PREFIX_MOD + "lapis_upgrade");
    }

    @Override
    public LapisUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new LapisUpgradeWrapper(stack, storage, consumer);
    }
}
