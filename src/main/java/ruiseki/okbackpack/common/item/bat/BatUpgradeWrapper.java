package ruiseki.okbackpack.common.item.bat;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.PotionEffectUpgradeWrapper;

public class BatUpgradeWrapper extends PotionEffectUpgradeWrapper {

    private static final int EFFECT_DURATION = 220;

    public BatUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    protected PotionEffect[] getEffects() {
        return new PotionEffect[] { new PotionEffect(Potion.nightVision.id, EFFECT_DURATION, 0, true), };
    }
}
