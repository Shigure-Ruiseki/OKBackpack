package ruiseki.okbackpack.common.item.travelers.spider;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemSpiderUpgrade extends ItemTravelersUpgradeBase<SpiderUpgradeWrapper> {

    public ItemSpiderUpgrade() {
        super("spider_upgrade", "tooltip.backpack.spider_upgrade");
    }

    @Override
    public SpiderUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new SpiderUpgradeWrapper(stack, storage, consumer);
    }
}
