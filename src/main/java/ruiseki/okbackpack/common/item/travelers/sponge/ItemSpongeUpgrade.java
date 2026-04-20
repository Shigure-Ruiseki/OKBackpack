package ruiseki.okbackpack.common.item.travelers.sponge;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemSpongeUpgrade extends ItemTravelersUpgradeBase<SpongeUpgradeWrapper> {

    public ItemSpongeUpgrade() {
        super("sponge_upgrade", "tooltip.backpack.sponge_upgrade");
    }

    @Override
    public SpongeUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new SpongeUpgradeWrapper(stack, storage, consumer);
    }
}
