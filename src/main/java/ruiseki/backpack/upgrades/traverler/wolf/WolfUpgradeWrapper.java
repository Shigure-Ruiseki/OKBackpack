package ruiseki.backpack.upgrades.traverler.wolf;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class WolfUpgradeWrapper extends UpgradeWrapperBase<WolfUpgradeWrapper, ItemWolfUpgrade>
    implements ITickableUpgrade, ITravelersUpgrade {

    private static final double CALM_RANGE = 8.0D;
    private static final int STRENGTH_DURATION = 40;
    private static final int STRENGTH_AMPLIFIER = 2;

    public WolfUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (!(entity instanceof EntityPlayer player)) return;
        calmNearbyWolves(player);
        if (isFullMoonNight(player.worldObj)) {
            player
                .addPotionEffect(new PotionEffect(Potion.damageBoost.id, STRENGTH_DURATION, STRENGTH_AMPLIFIER, true));
        }
    }

    private void calmNearbyWolves(EntityPlayer player) {
        List<EntityWolf> wolves = player.worldObj
            .getEntitiesWithinAABB(EntityWolf.class, player.boundingBox.expand(CALM_RANGE, 4.0D, CALM_RANGE));
        for (EntityWolf wolf : wolves) {
            if (!wolf.isAngry()) continue;
            wolf.setAngry(false);
            wolf.setAttackTarget(null);
            wolf.setRevengeTarget(null);
        }
    }

    private boolean isFullMoonNight(World world) {
        return !world.isDaytime() && world.getMoonPhase() == 0;
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
