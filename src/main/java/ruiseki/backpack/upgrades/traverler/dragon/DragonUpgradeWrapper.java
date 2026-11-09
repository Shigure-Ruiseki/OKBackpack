package ruiseki.backpack.upgrades.traverler.dragon;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.backpack.upgrades.traverler.PotionEffectUpgradeWrapper;
import ruiseki.okstoragecore.api.IStorageWrapper;

public class DragonUpgradeWrapper extends PotionEffectUpgradeWrapper<DragonUpgradeWrapper, ItemDragonUpgrade>
    implements ITravelersUpgrade {

    private static final int EFFECT_DURATION = 40;

    public DragonUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    protected PotionEffect[] getEffects() {
        return new PotionEffect[] { new PotionEffect(Potion.fireResistance.id, EFFECT_DURATION, 0, true),
            new PotionEffect(Potion.damageBoost.id, EFFECT_DURATION, 0, true),
            new PotionEffect(Potion.waterBreathing.id, EFFECT_DURATION, 0, true),
            new PotionEffect(Potion.regeneration.id, EFFECT_DURATION, 0, true) };
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean hideSettingsTab() {
        return true;
    }
}
