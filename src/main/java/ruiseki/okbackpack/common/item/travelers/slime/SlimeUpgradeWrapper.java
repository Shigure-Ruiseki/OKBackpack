package ruiseki.okbackpack.common.item.travelers.slime;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITickable;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.datastructure.BlockPos;

public class SlimeUpgradeWrapper extends UpgradeWrapperBase implements ITickable, ITravelersUpgrade {

    private static final int SPEED_DURATION = 40;
    private static final int SPEED_AMPLIFIER = 1;
    // Counteracts normal ground friction closely enough to feel like ice without runaway acceleration.
    private static final double SLIPPERY_MOTION_MULTIPLIER = 1.82D;
    private static final double MAX_SLIPPERY_MOTION = 0.98D;

    public SlimeUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    public static boolean shouldApplyFullMoonSpeed(boolean isDaytime, int moonPhase) {
        return !isDaytime && moonPhase == 0;
    }

    public static double adjustSlipperyMotion(double motion) {
        if (motion == 0D) return 0D;

        double boosted = motion * SLIPPERY_MOTION_MULTIPLIER;
        if (motion > 0D) {
            return Math.min(boosted, MAX_SLIPPERY_MOTION);
        }
        return Math.max(boosted, -MAX_SLIPPERY_MOTION);
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (player.onGround) {
            player.motionX = adjustSlipperyMotion(player.motionX);
            player.motionZ = adjustSlipperyMotion(player.motionZ);
        }

        if (player.worldObj instanceof WorldServer worldServer && player.isSprinting()) {
            worldServer
                .func_147487_a("slime", player.posX, player.posY + 0.1D, player.posZ, 2, 0.25D, 0.0D, 0.25D, 0.0D);
        }

        if (shouldApplyFullMoonSpeed(player.worldObj.isDaytime(), player.worldObj.getMoonPhase())) {
            player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, SPEED_DURATION, SPEED_AMPLIFIER, true));
        }
        return false;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        return false;
    }
}
