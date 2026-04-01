package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IPickupUpgrade;

public class PickupUpgradeWrapper extends BasicUpgradeWrapper implements IPickupUpgrade {

    public PickupUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
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
