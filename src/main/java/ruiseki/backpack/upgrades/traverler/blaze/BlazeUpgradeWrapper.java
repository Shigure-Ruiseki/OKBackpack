package ruiseki.backpack.upgrades.traverler.blaze;

import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import ruiseki.backpack.api.ILivingFallUpgrade;
import ruiseki.backpack.api.ILivingHurtUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class BlazeUpgradeWrapper extends UpgradeWrapperBase<BlazeUpgradeWrapper, ItemBlazeUpgrade>
    implements ILivingHurtUpgrade, ILivingFallUpgrade {

    public BlazeUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public float onLivingHurt(EntityLivingBase entity, DamageSource source, float amount) {
        return isBlazeFireballDamage(source) ? 0 : amount;
    }

    public boolean isBlazeFireballDamage(DamageSource source) {
        return source.getSourceOfDamage() instanceof EntitySmallFireball && source.getEntity() instanceof EntityBlaze;
    }

    @Override
    public float onLivingFall(EntityLivingBase entity, float distance) {
        return 0;
    }
}
