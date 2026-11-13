package ruiseki.backpack.upgrades.traverler.wither;

import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import ruiseki.backpack.api.IAttackEntityResponseUpgrade;
import ruiseki.backpack.api.ILivingHurtResponseUpgrade;
import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class WitherUpgradeWrapper extends UpgradeWrapperBase<WitherUpgradeWrapper, ItemWitherUpgrade>
    implements ITravelersUpgrade, ILivingHurtResponseUpgrade, IAttackEntityResponseUpgrade {

    public WitherUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public boolean hideSettingsTab() {
        return true;
    }

    @Override
    public boolean onLivingHurt(LivingHurtEvent event) {
        if (!isEnabled() || event.source != DamageSource.wither) return false;
        event.ammount = 0;
        event.setCanceled(true);
        return false;
    }

    @Override
    public boolean onAttackEntity(AttackEntityEvent event) {
        if (!(event.target instanceof EntityLivingBase livingTarget) || !isEnabled()) return false;
        livingTarget.addPotionEffect(new PotionEffect(Potion.wither.id, 60, 1, true));
        return false;
    }
}
