package ruiseki.okbackpack.common.item.travelers.ghast;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemGhastUpgrade extends ItemTravelersUpgradeBase<GhastUpgradeWrapper> {

    public ItemGhastUpgrade() {
        super("ghast_upgrade", "tooltip.backpack.ghast_upgrade");
    }

    @Override
    public GhastUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new GhastUpgradeWrapper(stack, storage, consumer);
    }
}
