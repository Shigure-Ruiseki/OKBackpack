package ruiseki.backpack.upgrades.traverler.creeper;

import java.util.function.Consumer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import ruiseki.backpack.api.ILivingHurtResponseUpgrade;
import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class CreeperUpgradeWrapper extends UpgradeWrapperBase<CreeperUpgradeWrapper, ItemCreeperUpgrade>
    implements ILivingHurtResponseUpgrade, ITravelersUpgrade {

    public static final int INTERVAL = 20 * 60 * 15;

    public CreeperUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public boolean onLivingHurt(LivingHurtEvent event) {
        Entity entity = event.entity;
        if (this.isInCooldown(entity.worldObj)) return false;
        if (!(entity instanceof EntityPlayer player)) return false;
        if (event.source == DamageSource.outOfWorld) return false;
        if (event.ammount < player.getHealth()) return false;

        player.setHealth(1.0F);
        player.extinguish();
        player.hurtResistantTime = 20;

        player.addPotionEffect(new PotionEffect(Potion.field_76444_x.id, 2400, 0, true));
        player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 100, 1, true));
        player.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 2400, 0, true));

        this.playRescueExplosion(player);

        event.ammount = 0;
        event.setCanceled(true);

        this.setCooldown(entity.worldObj, INTERVAL);
        return false;
    }

    private void playRescueExplosion(EntityPlayer player) {
        player.worldObj.playSoundAtEntity(player, "random.explode", 1.0F, 1.0F);

        if (player.worldObj instanceof WorldServer worldServer) {
            worldServer.func_147487_a(
                "hugeexplosion",
                player.posX,
                player.posY + player.height * 0.5D,
                player.posZ,
                1,
                0.0D,
                0.0D,
                0.0D,
                0.0D);
        }
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
