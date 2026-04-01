package ruiseki.okbackpack.common.item.wrapper;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

import org.joml.Vector3d;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IMagnetUpgrade;
import ruiseki.okbackpack.config.ModConfig;
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
        markDirty();
    }

    @Override
    public boolean isCollectExp() {
        return ItemNBTHelpers.getBoolean(upgrade, MAG_EXP_TAG, true);
    }

    @Override
    public void setCollectExp(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, MAG_EXP_TAG, enabled);
        markDirty();
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
}
