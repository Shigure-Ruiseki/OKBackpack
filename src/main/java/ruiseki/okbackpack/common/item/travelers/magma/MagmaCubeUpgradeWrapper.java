package ruiseki.okbackpack.common.item.travelers.magma;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.PotionEffectUpgradeWrapper;

public class MagmaCubeUpgradeWrapper extends PotionEffectUpgradeWrapper implements ITravelersUpgrade {

    private static final int EFFECT_DURATION = 40;

    public MagmaCubeUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    protected PotionEffect[] getEffects() {
        return new PotionEffect[] { new PotionEffect(Potion.fireResistance.id, EFFECT_DURATION, 0, true) };
    }
}
