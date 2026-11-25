package ruiseki.okbackpack.common.item.travelers.dragon;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.PotionEffectUpgradeWrapper;

public class DragonUpgradeWrapper extends PotionEffectUpgradeWrapper implements ITravelersUpgrade {

    private static final int EFFECT_DURATION = 40;

    public DragonUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    protected PotionEffect[] getEffects() {
        return new PotionEffect[] { new PotionEffect(Potion.fireResistance.id, EFFECT_DURATION, 0, true),
            new PotionEffect(Potion.damageBoost.id, EFFECT_DURATION, 0, true),
            new PotionEffect(Potion.waterBreathing.id, EFFECT_DURATION, 0, true),
            new PotionEffect(Potion.regeneration.id, EFFECT_DURATION, 0, true) };
    }
}
