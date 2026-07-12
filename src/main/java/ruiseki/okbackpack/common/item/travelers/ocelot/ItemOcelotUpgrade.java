package ruiseki.okbackpack.common.item.travelers.ocelot;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.travelers.ItemTravelersUpgradeBase;

public class ItemOcelotUpgrade extends ItemTravelersUpgradeBase<OcelotUpgradeWrapper> {

    public ItemOcelotUpgrade() {
        super("tooltip.backpack.ocelot_upgrade");
        setTextureName(Reference.PREFIX_MOD + "ocelot_upgrade");
    }

    @Override
    public OcelotUpgradeWrapper createWrapper(ItemStack stack, IStorageWrapper storage, Consumer<ItemStack> consumer) {
        return new OcelotUpgradeWrapper(stack, storage, consumer);
    }
}
