package ruiseki.okbackpack.common.item.travelers.quiver;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemQuiverUpgrade extends ItemTravelersUpgradeBase<QuiverUpgradeWrapper> {

    public ItemQuiverUpgrade() {
        super("quiver_upgrade", "tooltip.backpack.quiver_upgrade");
    }

    @Override
    public QuiverUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new QuiverUpgradeWrapper(stack, storage, consumer);
    }
}
