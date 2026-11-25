package ruiseki.okbackpack.common.item;

import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITickable;
import ruiseki.okcore.datastructure.BlockPos;

public abstract class PotionEffectUpgradeWrapper extends UpgradeWrapperBase implements ITickable {

    public PotionEffectUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    protected abstract PotionEffect[] getEffects();

    @Override
    public boolean tick(EntityPlayer player) {
        for (PotionEffect effect : getEffects()) {
            player.addPotionEffect(new PotionEffect(effect));
        }
        return false;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        return false;
    }
}
