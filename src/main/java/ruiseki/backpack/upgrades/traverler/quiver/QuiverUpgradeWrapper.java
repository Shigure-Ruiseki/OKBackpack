package ruiseki.backpack.upgrades.traverler.quiver;

import java.util.function.Consumer;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;

import ruiseki.backpack.api.IArrowResponseUpgrade;
import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class QuiverUpgradeWrapper extends UpgradeWrapperBase<QuiverUpgradeWrapper, ItemQuiverUpgrade>
    implements ITravelersUpgrade, IArrowResponseUpgrade {

    private static final ItemStack SINGLE_ARROW = new ItemStack(Items.arrow, 1);

    public QuiverUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean hideSettingsTab() {
        return true;
    }

    @Override
    public boolean onArrowNock(ArrowNockEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player == null) return false;
        if (player.capabilities.isCreativeMode) return false;
        if (player.inventory.hasItem(Items.arrow)) return false;
        player.setItemInUse(event.result, event.result.getMaxItemUseDuration());
        event.setCanceled(true);
        return true;
    }

    @Override
    public boolean onArrowLoose(ArrowLooseEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player == null) return false;
        if (player.capabilities.isCreativeMode) return false;
        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.infinity.effectId, event.bow) > 0) return false;
        event.setCanceled(true);
        shootArrowFromQuiver(player, event.bow, event.charge);
        return true;
    }

    public void shootArrowFromQuiver(EntityPlayer player, ItemStack bow, int charge) {
        float velocity = getArrowVelocity(charge);
        if ((double) velocity < 0.1D) return;

        EntityArrow arrow = new EntityArrow(player.worldObj, player, velocity * 2.0F);
        if (velocity == 1.0F) {
            arrow.setIsCritical(true);
        }

        int power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, bow);
        if (power > 0) {
            arrow.setDamage(arrow.getDamage() + power * 0.5D + 0.5D);
        }

        int punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, bow);
        if (punch > 0) {
            arrow.setKnockbackStrength(punch);
        }

        if (EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, bow) > 0) {
            arrow.setFire(100);
        }

        bow.damageItem(1, player);
        storageWrapper.getInventoryHandler()
            .extractItem(SINGLE_ARROW, false);

        player.worldObj.playSoundAtEntity(
            player,
            "random.bow",
            1.0F,
            1.0F / (player.getRNG()
                .nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
        player.worldObj.spawnEntityInWorld(arrow);
    }

    public float getArrowVelocity(int charge) {
        float velocity = charge / 20.0F;
        velocity = (velocity * velocity + velocity * 2.0F) / 3.0F;
        if (velocity > 1.0F) {
            velocity = 1.0F;
        }
        return velocity;
    }
}
