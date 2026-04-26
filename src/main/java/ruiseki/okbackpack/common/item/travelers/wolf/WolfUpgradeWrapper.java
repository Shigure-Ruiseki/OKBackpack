package ruiseki.okbackpack.common.item.travelers.wolf;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITickable;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.datastructure.BlockPos;

public class WolfUpgradeWrapper extends UpgradeWrapperBase implements ITickable, ITravelersUpgrade {

    private static final double CALM_RANGE = 8.0D;
    private static final int STRENGTH_DURATION = 40;
    private static final int STRENGTH_AMPLIFIER = 2;

    public WolfUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean tick(EntityPlayer player) {
        calmNearbyWolves(player);

        if (isFullMoonNight(player.worldObj)) {
            player
                .addPotionEffect(new PotionEffect(Potion.damageBoost.id, STRENGTH_DURATION, STRENGTH_AMPLIFIER, true));
        }
        return false;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        return false;
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
}
