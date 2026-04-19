package ruiseki.okbackpack.common.item.travelers.glowstone;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ILightUpgrade;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;

public class GlowstoneUpgradeWrapper extends UpgradeWrapperBase implements ITravelersUpgrade, ILightUpgrade {

    public GlowstoneUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public int getLightLevel() {
        return 15;
    }
}
