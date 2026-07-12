package ruiseki.okbackpack.common.item.travelers.wolf;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemWolfUpgrade extends ItemTravelersUpgradeBase<WolfUpgradeWrapper> {

    public ItemWolfUpgrade() {
        super("tooltip.backpack.wolf_upgrade", "tooltip.backpack.wolf_upgrade.1");
        setTextureName(Reference.PREFIX_MOD + "wolf_upgrade");
    }

    @Override
    public WolfUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new WolfUpgradeWrapper(stack, storage, consumer);
    }
}
