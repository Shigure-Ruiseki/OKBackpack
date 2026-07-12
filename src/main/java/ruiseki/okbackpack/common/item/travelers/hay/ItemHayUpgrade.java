package ruiseki.okbackpack.common.item.travelers.hay;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemHayUpgrade extends ItemTravelersUpgradeBase<HayUpgradeWrapper> {

    public ItemHayUpgrade() {
        super("tooltip.backpack.hay_upgrade", "tooltip.backpack.hay_upgrade.1");
        setTextureName(Reference.PREFIX_MOD + "hay_upgrade");
    }

    @Override
    public HayUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new HayUpgradeWrapper(stack, storage, consumer);
    }
}
