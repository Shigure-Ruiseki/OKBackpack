package ruiseki.okbackpack.common.item.travelers.redstone;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IRedstoneUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;

public class RedstoneUpgradeWrapper extends UpgradeWrapperBase implements IRedstoneUpgrade {

    public RedstoneUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }
}
