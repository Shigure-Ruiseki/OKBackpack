package ruiseki.backpack.upgrades.traverler.rainbow;

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
import ruiseki.backpack.upgrades.traverler.PotionEffectUpgradeWrapper;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;

public class RainbowUpgradeWrapper extends PotionEffectUpgradeWrapper<RainbowUpgradeWrapper, ItemRainbowUpgrade>
    implements ITravelersUpgrade, ITickableUpgrade, ILivingFallResponseUpgrade {

    private static final int EFFECT_DURATION = 40;
    private static final int EFFECT_AMPLIFIER = 1;

    public RainbowUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    protected PotionEffect[] getEffects() {
        return new PotionEffect[] { new PotionEffect(Potion.jump.id, EFFECT_DURATION, EFFECT_AMPLIFIER, true),
            new PotionEffect(Potion.moveSpeed.id, EFFECT_DURATION, EFFECT_AMPLIFIER, true), };
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        super.tick(entity, world, pos);

        if (!(entity instanceof EntityPlayer player)) return;
        if (player.isSprinting() && world instanceof WorldServer worldServer) {
            double noteColor = player.getRNG()
                .nextInt(25) / 24.0;
            worldServer.func_147487_a("note", player.posX, player.posY + 0.1, player.posZ, 1, 0.3, 0.0, 0.3, noteColor);
        }
    }

    @Override
    public boolean onLivingFall(LivingFallEvent event) {
        if (!(event.entity instanceof EntityPlayer player)) return false;
        player.fallDistance = 0F;
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
