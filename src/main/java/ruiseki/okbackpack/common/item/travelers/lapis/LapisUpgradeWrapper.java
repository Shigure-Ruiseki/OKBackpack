package ruiseki.okbackpack.common.item.travelers.lapis;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;

public class LapisUpgradeWrapper extends UpgradeWrapperBase implements ITravelersUpgrade {

    public LapisUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }
}
