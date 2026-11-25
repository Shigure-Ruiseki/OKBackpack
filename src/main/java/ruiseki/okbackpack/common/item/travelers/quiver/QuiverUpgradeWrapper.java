package ruiseki.okbackpack.common.item.travelers.quiver;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;

public class QuiverUpgradeWrapper extends UpgradeWrapperBase implements ITravelersUpgrade {

    public QuiverUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }
}
