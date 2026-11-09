package ruiseki.backpack.upgrades.traverler.creeper;

import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;

import ruiseki.backpack.api.ILivingHurtUpgrade;
import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class CreeperUpgradeWrapper extends UpgradeWrapperBase<CreeperUpgradeWrapper, ItemCreeperUpgrade>
    implements ILivingHurtUpgrade, ITravelersUpgrade {

    public static final int INTERVAL = 20 * 60 * 15;

    public CreeperUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public float onLivingHurt(EntityLivingBase entity, DamageSource source, float amount) {
        if (this.isInCooldown(entity.worldObj)) return amount;
        if (!(entity instanceof EntityPlayer player)) return amount;
        if (source == DamageSource.outOfWorld) return amount;
        if (amount < player.getHealth()) return amount;

        player.setHealth(1.0F);
        player.extinguish();
        player.hurtResistantTime = 20;

        player.addPotionEffect(new PotionEffect(Potion.field_76444_x.id, 2400, 0, true));
        player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 100, 1, true));
        player.addPotionEffect(new PotionEffect(Potion.fireResistance.id, 2400, 0, true));

        this.playRescueExplosion(player);

        this.setCooldown(entity.worldObj, INTERVAL);
        return 0;
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
}
