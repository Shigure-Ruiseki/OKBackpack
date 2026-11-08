package ruiseki.backpack.upgrades.traverler.bat;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.backpack.upgrades.traverler.PotionEffectUpgradeWrapper;
import ruiseki.okstoragecore.api.IStorageWrapper;

public class BatUpgradeWrapper extends PotionEffectUpgradeWrapper<BatUpgradeWrapper, ItemBatUpgrade>
    implements ITravelersUpgrade {

    private static final int EFFECT_DURATION = 220;

    protected BatUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    protected PotionEffect[] getEffects() {
        return new PotionEffect[] { new PotionEffect(Potion.nightVision.id, EFFECT_DURATION, 0, true), };
    }
}
