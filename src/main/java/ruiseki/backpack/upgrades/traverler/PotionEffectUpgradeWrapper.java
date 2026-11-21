package ruiseki.backpack.upgrades.traverler;

import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public abstract class PotionEffectUpgradeWrapper<W extends PotionEffectUpgradeWrapper<W, T>, T extends ItemUpgradeBase<W>>
    extends UpgradeWrapperBase<W, T> implements ITickableUpgrade {

    private static final int EFFECT_COOLDOWN_TIME = 20;

    protected PotionEffectUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (entity == null || isInCooldown(world)) return;
        for (PotionEffect effect : getEffects()) {
            entity.addPotionEffect(new PotionEffect(effect));
        }
        setCooldown(world, EFFECT_COOLDOWN_TIME);
    }

    protected abstract PotionEffect[] getEffects();
}
