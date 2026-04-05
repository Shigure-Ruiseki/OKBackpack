package ruiseki.okbackpack.common.item.pickup;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IPickupUpgrade;
import ruiseki.okbackpack.common.item.AdvancedUpgradeWrapper;

public class AdvancedPickupUpgradeWrapper extends AdvancedUpgradeWrapper implements IPickupUpgrade {

    public AdvancedPickupUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage,
        Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.advanced_pickup_settings";
    }

    @Override
    public boolean canPickup(ItemStack stack) {
        return checkFilter(stack);
    }
}
