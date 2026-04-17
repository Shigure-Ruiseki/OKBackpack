package ruiseki.okbackpack.common.item.travelers.ocelot;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
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

public class OcelotUpgradeWrapper extends UpgradeWrapperBase implements ITickable, ITravelersUpgrade {

    private static final double HOSTILE_RANGE = 8.0D;
    private static final int SPEED_DURATION = 40;

    public OcelotUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (hasNearbyHostile(player)) {
            player.addPotionEffect(new PotionEffect(Potion.moveSpeed.id, SPEED_DURATION, 0, true));
        }
        return false;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        return false;
    }

    private boolean hasNearbyHostile(EntityPlayer player) {
        List<EntityLivingBase> entities = player.worldObj.getEntitiesWithinAABB(
            EntityLivingBase.class,
            player.boundingBox.expand(HOSTILE_RANGE, 4.0D, HOSTILE_RANGE));
        for (EntityLivingBase entity : entities) {
            if (entity == player || !entity.isEntityAlive()) continue;
            if (entity instanceof IMob) {
                return true;
            }
        }
        return false;
    }
}
