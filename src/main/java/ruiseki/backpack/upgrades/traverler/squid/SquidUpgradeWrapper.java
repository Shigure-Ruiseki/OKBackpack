package ruiseki.backpack.upgrades.traverler.squid;

import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.backpack.upgrades.traverler.PotionEffectUpgradeWrapper;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;

public class SquidUpgradeWrapper extends PotionEffectUpgradeWrapper<SquidUpgradeWrapper, ItemSquidUpgrade>
    implements ITickableUpgrade, ITravelersUpgrade {

    private static final int EFFECT_DURATION = 40;

    public SquidUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    protected PotionEffect[] getEffects() {
        return new PotionEffect[] { new PotionEffect(Potion.waterBreathing.id, EFFECT_DURATION, 0, true),
            new PotionEffect(Potion.nightVision.id, EFFECT_DURATION, 0, true), };
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (entity == null || !entity.isInWater()) return;
        super.tick(entity, world, pos);
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
