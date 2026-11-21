package ruiseki.backpack.upgrades.traverler.slime;

import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingFallEvent;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.api.ILivingFallResponseUpgrade;
import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class SlimeUpgradeWrapper extends UpgradeWrapperBase<SlimeUpgradeWrapper, ItemSlimeUpgrade>
    implements ITravelersUpgrade, ILivingFallResponseUpgrade, ITickableUpgrade {

    private static final int SPEED_DURATION = 40;
    private static final int SPEED_AMPLIFIER = 1;
    private static final double GRAVITY = 0.08D;
    private static final double DRAG = 0.98D;
    private static final int MAX_FALL_TICKS = 2000;

    public SlimeUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
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
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (!(entity instanceof EntityPlayer player)) return;
        if (player.worldObj instanceof WorldServer worldServer && player.isSprinting()) {
            worldServer
                .func_147487_a("slime", player.posX, player.posY + 0.1D, player.posZ, 2, 0.25D, 0.0D, 0.25D, 0.0D);
        }

        if (shouldApplyFullMoonSpeed(player.worldObj.isDaytime(), player.worldObj.getMoonPhase())) {
            player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, SPEED_DURATION, SPEED_AMPLIFIER, true));
        }
    }

    @Override
    public boolean onLivingFall(LivingFallEvent event) {
        if (!(event.entity instanceof EntityPlayer player)) return false;
        player.fallDistance = 0F;
        event.setCanceled(true);
        if (player.isSneaking()) return false;

        double bounceVelocity = calculateBounceVelocity(event.distance);
        if (bounceVelocity <= 0D) return false;

        double deltaY = bounceVelocity - player.motionY;
        player.addVelocity(0D, deltaY, 0D);
        player.onGround = false;
        player.isAirBorne = true;
        player.velocityChanged = true;

        return false;
    }

    public static boolean shouldApplyFullMoonSpeed(boolean isDaytime, int moonPhase) {
        return !isDaytime && moonPhase == 0;
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
