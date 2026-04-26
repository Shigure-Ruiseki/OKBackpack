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
    private static final double GRAVITY = 0.08D;
    private static final double DRAG = 0.98D;
    private static final int MAX_FALL_TICKS = 2000;

    public SlimeUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    public static boolean shouldApplyFullMoonSpeed(boolean isDaytime, int moonPhase) {
        return !isDaytime && moonPhase == 0;
    }

    @Override
    public boolean tick(EntityPlayer player) {
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

    public static double calculateBounceVelocity(double fallDistance) {
        if (fallDistance <= 0D) return 0D;

        double remaining = fallDistance;
        double velocityY = 0D;

        for (int i = 0; i < MAX_FALL_TICKS && remaining > 0D; i++) {
            velocityY = (velocityY - GRAVITY) * DRAG;
            remaining += velocityY;
        }

        return Math.max(0D, -velocityY);
    }
}
