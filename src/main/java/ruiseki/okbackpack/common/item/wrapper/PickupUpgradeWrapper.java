package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

public class PickupUpgradeWrapper extends BasicUpgradeWrapper implements IPickupUpgrade {

    public PickupUpgradeWrapper(ItemStack upgrade) {
        super(upgrade);
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
