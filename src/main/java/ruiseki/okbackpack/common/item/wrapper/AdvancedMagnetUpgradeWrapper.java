package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IMagnetUpgrade;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class AdvancedMagnetUpgradeWrapper extends AdvancedPickupUpgradeWrapper implements IMagnetUpgrade {

    public AdvancedMagnetUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.advanced_magnet_settings";
    }

    @Override
    public boolean isCollectItem() {
        return ItemNBTHelpers.getBoolean(upgrade, MAG_ITEM_TAG, true);
    }

    @Override
    public void setCollectItem(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, MAG_ITEM_TAG, enabled);
        markDirty();
    }

    @Override
    public boolean isCollectExp() {
        return ItemNBTHelpers.getBoolean(upgrade, MAG_EXP_TAG, true);
    }

    @Override
    public void setCollectExp(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, MAG_EXP_TAG, enabled);
        markDirty();
    }

    @Override
    public boolean canCollectItem(ItemStack stack) {
        return checkFilter(stack);
    }
}
