package ruiseki.okbackpack.common.item.rainbow;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.WorldServer;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.common.item.PotionEffectUpgradeWrapper;

public class RainbowUpgradeWrapper extends PotionEffectUpgradeWrapper {

    private static final int EFFECT_DURATION = 40;
    private static final int EFFECT_AMPLIFIER = 1;

    public RainbowUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    protected PotionEffect[] getEffects() {
        return new PotionEffect[] { new PotionEffect(Potion.jump.id, EFFECT_DURATION, EFFECT_AMPLIFIER, true),
            new PotionEffect(Potion.moveSpeed.id, EFFECT_DURATION, EFFECT_AMPLIFIER, true), };
    }

    @Override
    public boolean tick(EntityPlayer player) {
        super.tick(player);

        if (player.isSprinting() && player.worldObj instanceof WorldServer worldServer) {
            double noteColor = player.getRNG()
                .nextInt(25) / 24.0;
            worldServer.func_147487_a("note", player.posX, player.posY + 0.1, player.posZ, 1, 0.3, 0.0, 0.3, noteColor);
        }

        return false;
    }
}
