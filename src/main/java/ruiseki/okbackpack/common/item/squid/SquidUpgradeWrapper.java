package ruiseki.okbackpack.common.item.squid;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.PotionEffectUpgradeWrapper;

public class SquidUpgradeWrapper extends PotionEffectUpgradeWrapper {

    private static final int EFFECT_DURATION = 40;

    public SquidUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    protected PotionEffect[] getEffects() {
        return new PotionEffect[] { new PotionEffect(Potion.waterBreathing.id, EFFECT_DURATION, 0, true),
            new PotionEffect(Potion.nightVision.id, EFFECT_DURATION, 0, true), };
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (!player.isInWater()) return false;
        return super.tick(player);
    }
}
