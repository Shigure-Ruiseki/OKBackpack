package ruiseki.backpack.upgrades.traverler.blaze;

import java.util.function.Consumer;

import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import ruiseki.backpack.api.ILivingFallResponseUpgrade;
import ruiseki.backpack.api.ILivingHurtResponseUpgrade;
import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class BlazeUpgradeWrapper extends UpgradeWrapperBase<BlazeUpgradeWrapper, ItemBlazeUpgrade>
    implements ILivingHurtResponseUpgrade, ILivingFallResponseUpgrade, ITravelersUpgrade {

    public BlazeUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public boolean onLivingHurt(LivingHurtEvent event) {
        if (!isBlazeFireballDamage(event.source)) return false;
        event.ammount = 0;
        event.setCanceled(true);
        return false;
    }

    public boolean isBlazeFireballDamage(DamageSource source) {
        return source.getSourceOfDamage() instanceof EntitySmallFireball && source.getEntity() instanceof EntityBlaze;
    }

    @Override
    public boolean onLivingFall(LivingFallEvent event) {
        event.distance = 0;
        event.setCanceled(true);
        return false;
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
