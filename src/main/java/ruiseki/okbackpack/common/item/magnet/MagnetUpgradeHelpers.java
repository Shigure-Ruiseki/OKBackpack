package ruiseki.okbackpack.common.item.magnet;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerPickupXpEvent;

import org.joml.Vector3d;

import com.cleanroommc.modularui.factory.inventory.InventoryTypes;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IMagnetUpgrade;
import ruiseki.okbackpack.common.event.BackpackEventHandler;
import ruiseki.okbackpack.common.helpers.BaublesHelpers;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemHandlerHelpers;
import ruiseki.okcore.item.capability.wrapper.PlayerMainInvWrapper;

public class MagnetUpgradeHelpers {

    private static final int MAX_MAGNET_ENTITIES = 20;
    private static final double STORAGE_PICKUP_DISTANCE_SQ = 2.25D;

    private MagnetUpgradeHelpers() {}

    public static boolean tickPlayer(IMagnetUpgrade upgrade, EntityPlayer player) {
        if (player == null || player.isDead || player.isSneaking() || player.ticksExisted % 2 != 0) return false;

        World world = player.worldObj;
        if (world.isRemote) return false;

        AxisAlignedBB aabb = createRange(player.posX, player.posY, player.posZ);
        List<Entity> entities = upgrade.getMagnetEntities(world, aabb);
        if (entities.isEmpty()) return false;

        int processed = 0;

        for (Entity entity : entities) {
            if (entity.isDead) continue;
            if (processed >= MAX_MAGNET_ENTITIES) break;
            processed++;
            if (!isClosestPlayer(player, entity)) continue;

            if (!world.isRemote && entity instanceof EntityItem itemEntity) {
                ItemStack stack = itemEntity.getEntityItem();
                if (stack == null || stack.stackSize <= 0) continue;

                if (itemEntity.delayBeforeCanPickup > 0) itemEntity.delayBeforeCanPickup = 0;

                ItemStack remaining = insertIntoPlayer(player, stack);
                int remainingSize = remaining == null ? 0 : remaining.stackSize;

                if (remainingSize <= 0) {
                    itemEntity.setDead();
                } else if (remainingSize != stack.stackSize) {
                    itemEntity.setEntityItemStack(remaining.copy());
                }
                continue;
            }

            if (entity instanceof EntityXPOrb xpOrb) collectExperience(player, xpOrb);
        }

        return false;
    }

    public static boolean tickStorage(IMagnetUpgrade upgrade, IStorageWrapper storage, World world, BlockPos pos) {
        if (world == null || pos == null || world.getWorldTime() % 2 != 0) return false;

        double centerX = pos.x + 0.5D;
        double centerY = pos.y + 0.5D;
        double centerZ = pos.z + 0.5D;
        List<Entity> entities = upgrade.getMagnetEntities(world, createRange(centerX, centerY, centerZ));
        if (entities.isEmpty()) return false;

        boolean storageLoaded = false;
        boolean storageChanged = false;
        int processed = 0;

        for (Entity entity : entities) {
            if (entity.isDead) continue;
            if (processed >= MAX_MAGNET_ENTITIES) break;
            processed++;

            if (!world.isRemote && entity instanceof EntityItem itemEntity) {
                ItemStack stack = itemEntity.getEntityItem();
                if (stack == null || stack.stackSize <= 0) continue;
                if (itemEntity.delayBeforeCanPickup > 0) itemEntity.delayBeforeCanPickup = 0;

                double distanceSq = entity.getDistanceSq(centerX, centerY + 0.25D, centerZ);
                if (distanceSq < STORAGE_PICKUP_DISTANCE_SQ) {
                    if (!storageLoaded) {
                        storage.readFromItem();
                        storageLoaded = true;
                    }

                    ItemStack remaining = insertIntoStorage(storage, stack);
                    int remainingSize = remaining == null ? 0 : remaining.stackSize;
                    if (remainingSize < stack.stackSize) storageChanged = true;

                    if (remainingSize <= 0) {
                        itemEntity.setDead();
                    } else if (remainingSize != stack.stackSize) {
                        itemEntity.setEntityItemStack(remaining.copy());
                    }
                    continue;
                }
            }

            upgrade.setEntityMotionFromVector(entity, new Vector3d(centerX, centerY + 0.25D, centerZ), 0.45F);
        }

        if (storageChanged) storage.writeToItem();
        return storageChanged;
    }

    private static AxisAlignedBB createRange(double x, double y, double z) {
        return AxisAlignedBB.getBoundingBox(
            x - GeneralConfig.magnetRange,
            y - GeneralConfig.magnetRange,
            z - GeneralConfig.magnetRange,
            x + GeneralConfig.magnetRange,
            y + GeneralConfig.magnetRange,
            z + GeneralConfig.magnetRange);
    }

    private static boolean isClosestPlayer(EntityPlayer player, Entity entity) {
        if (player.worldObj.playerEntities.size() < 2) return true;

        EntityPlayer closestPlayer = player.worldObj.getClosestPlayerToEntity(entity, GeneralConfig.magnetRange);
        return closestPlayer == player;
    }

    private static ItemStack insertIntoStorage(IStorageWrapper storage, ItemStack stack) {
        ItemStack remaining = storage.insertItem(stack.copy(), false);
        if (remaining == null || remaining.stackSize <= 0) return null;

        int remainingSize = Math.min(stack.stackSize, remaining.stackSize);
        ItemStack safeRemaining = remaining.copy();
        safeRemaining.stackSize = remainingSize;
        return safeRemaining;
    }

    private static ItemStack insertIntoPlayer(EntityPlayer player, ItemStack stack) {
        ItemStack remaining = BackpackEventHandler
            .attemptPickup(player, BaublesHelpers.getBaubles(player), stack.copy(), InventoryTypes.BAUBLES);
        if (remaining != null && remaining.stackSize > 0) {
            remaining = BackpackEventHandler.attemptPickup(player, player.inventory, remaining, InventoryTypes.PLAYER);
        }
        if (remaining != null && remaining.stackSize > 0) {
            remaining = ItemHandlerHelpers
                .insertItemStacked(new PlayerMainInvWrapper(player.inventory), remaining.copy(), false);
        }

        if (remaining == null || remaining.stackSize <= 0) return null;
        int remainingSize = Math.min(stack.stackSize, remaining.stackSize);
        ItemStack safeRemaining = remaining.copy();
        safeRemaining.stackSize = remainingSize;
        return safeRemaining;
    }

    private static void collectExperience(EntityPlayer player, EntityXPOrb xpOrb) {
        if (xpOrb.field_70532_c != 0 || MinecraftForge.EVENT_BUS.post(new PlayerPickupXpEvent(player, xpOrb))) return;

        player.onItemPickup(xpOrb, 1);
        player.addExperience(xpOrb.xpValue);
        xpOrb.setDead();
    }
}
