package ruiseki.okbackpack.common.item.travelers.redstone;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IRedstoneUpgrade;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;

public class RedstoneUpgradeWrapper extends UpgradeWrapperBase implements ITravelersUpgrade, IRedstoneUpgrade {

    public RedstoneUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public int getRedstonePower() {
        return 15;
    }
}
