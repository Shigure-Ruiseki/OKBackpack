package ruiseki.okbackpack.common.item.wrapper;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import org.joml.Vector3d;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IMagnetUpgrade;
import ruiseki.okbackpack.config.ModConfig;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class AdvancedMagnetUpgradeWrapper extends AdvancedPickupUpgradeWrapper implements IMagnetUpgrade {

    public AdvancedMagnetUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.advanced_magnet_settings";
    }

    @Override
    public boolean isCollectItem() {
        return ItemNBTHelpers.getBoolean(upgrade, MAG_ITEM_TAG, true);
    }

    @Override
    public void setCollectItem(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, MAG_ITEM_TAG, enabled);
    }

    @Override
    public boolean isCollectExp() {
        return ItemNBTHelpers.getBoolean(upgrade, MAG_EXP_TAG, true);
    }

    @Override
    public void setCollectExp(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, MAG_EXP_TAG, enabled);
    }

    @Override
    public boolean canCollectItem(ItemStack stack) {
        return checkFilter(stack);
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (player.ticksExisted % 2 != 0) return false;

        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
            player.posX - ModConfig.magnetRange,
            player.posY - ModConfig.magnetRange,
            player.posZ - ModConfig.magnetRange,
            player.posX + ModConfig.magnetRange,
            player.posY + ModConfig.magnetRange,
            player.posZ + ModConfig.magnetRange);

        List<Entity> entities = getMagnetEntities(player.worldObj, aabb);
        if (entities.isEmpty()) return false;

        int pulled = 0;
        for (Entity entity : entities) {
            if (pulled++ > 20) {
                break;
            }
            Vector3d target = new Vector3d(
                player.posX,
                player.posY - (player.worldObj.isRemote ? 1.62 : 0) + 0.75,
                player.posZ);
            setEntityMotionFromVector(entity, target, 0.45F);
        }

        return false;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        if (world.getWorldTime() % 2 != 0) return false;

        double centerX = pos.x + 0.5;
        double centerY = pos.y + 0.5;
        double centerZ = pos.z + 0.5;

        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
            centerX - ModConfig.magnetRange,
            centerY - ModConfig.magnetRange,
            centerZ - ModConfig.magnetRange,
            centerX + ModConfig.magnetRange,
            centerY + ModConfig.magnetRange,
            centerZ + ModConfig.magnetRange);

        List<Entity> entities = getMagnetEntities(world, aabb);
        if (entities.isEmpty()) return false;

        int pulled = 0;

        for (Entity entity : entities) {
            if (pulled++ > 20) break;

            double dx = centerX;
            double dy = centerY + 0.25;
            double dz = centerZ;

            if (!world.isRemote && entity instanceof EntityItem itemEntity) {

                if (itemEntity.delayBeforeCanPickup > 0) continue;

                ItemStack stack = itemEntity.getEntityItem();
                if (stack == null || !canCollectItem(stack)) continue;

                double distSq = entity.getDistanceSq(dx, dy, dz);

                if (distSq < 2.25) { // ~1.5 block
                    ItemStack remaining = storage.insertItem(stack, false);

                    if (remaining == null || remaining.stackSize <= 0) {
                        entity.setDead();
                    } else {
                        itemEntity.setEntityItemStack(remaining);
                    }

                    continue;
                }
            }

            setEntityMotionFromVector(entity, new Vector3d(dx, dy, dz), 0.45F);
        }

        return true;
    }
}
