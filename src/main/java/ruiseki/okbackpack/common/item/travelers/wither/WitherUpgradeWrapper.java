package ruiseki.okbackpack.common.item.travelers.wither;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IWitherUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class WitherUpgradeWrapper extends UpgradeWrapperBase implements IWitherUpgrade {

    public WitherUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean isEnabled() {
        return ItemNBTHelpers.getBoolean(upgrade, ENABLED_TAG, true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, ENABLED_TAG, enabled);
        save();
    }
}
