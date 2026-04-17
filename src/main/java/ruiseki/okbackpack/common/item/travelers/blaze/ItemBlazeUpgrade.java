package ruiseki.okbackpack.common.item.travelers.blaze;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemBlazeUpgrade extends ItemTravelersUpgradeBase<BlazeUpgradeWrapper> {

    public ItemBlazeUpgrade() {
        super("blaze_upgrade", "tooltip.backpack.blaze_upgrade");
    }

    @Override
    public BlazeUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new BlazeUpgradeWrapper(stack, storage, consumer);
    }
}
