package ruiseki.okbackpack.common.item.travelers.quartz;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemQuartzUpgrade extends ItemTravelersUpgradeBase<QuartzUpgradeWrapper> {

    public ItemQuartzUpgrade() {
        super("quartz_upgrade", "tooltip.backpack.quartz_upgrade");
    }

    @Override
    public QuartzUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new QuartzUpgradeWrapper(stack, storage, consumer);
    }
}
