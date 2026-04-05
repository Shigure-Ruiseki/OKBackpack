package ruiseki.okbackpack.common.item.pickup;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IPickupUpgrade;
import ruiseki.okbackpack.common.item.BasicUpgradeWrapper;

public class PickupUpgradeWrapper extends BasicUpgradeWrapper implements IPickupUpgrade {

    public PickupUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.pickup_settings";
    }

    @Override
    public boolean canPickup(ItemStack stack) {
        return checkFilter(stack);
    }
}
