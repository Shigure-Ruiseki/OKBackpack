package ruiseki.okbackpack.common.item.travelers.ghast;

import java.util.List;

import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.okbackpack.common.helpers.BackpackEntityHelper;

public final class GhastUpgradeSupport {

    private static final int GHAST_RETALIATE_TICKS = 200;
    private static final String GHAST_ANGER_PLAYER_TAG = "OKBGhastAngerPlayer";
    private static final String GHAST_ANGER_UNTIL_TAG = "OKBGhastAngerUntil";

    private GhastUpgradeSupport() {}

    public static boolean hasGhastUpgrade(EntityPlayer player) {
        return BackpackEntityHelper.visitBackpacks(
            player,
            context -> !context.wrapper()
                .gatherCapabilityUpgrades(GhastUpgradeWrapper.class)
                .isEmpty());
    }

    public static void markGhastRetaliation(EntityGhast ghast, EntityPlayer player) {
        ghast.getEntityData()
            .setString(
                GHAST_ANGER_PLAYER_TAG,
                player.getUniqueID()
                    .toString());
        ghast.getEntityData()
            .setLong(GHAST_ANGER_UNTIL_TAG, player.worldObj.getTotalWorldTime() + GHAST_RETALIATE_TICKS);
        ghast.setAttackTarget(player);
        ghast.setRevengeTarget(player);
    }

    public static boolean isGhastAggressiveToPlayer(EntityGhast ghast, EntityPlayer player, long worldTime) {
        NBTTagCompound data = ghast.getEntityData();
        long angerUntil = data.getLong(GHAST_ANGER_UNTIL_TAG);
        if (angerUntil <= worldTime) {
            data.removeTag(GHAST_ANGER_PLAYER_TAG);
            data.removeTag(GHAST_ANGER_UNTIL_TAG);
            return false;
        }
        return player.getUniqueID()
            .toString()
            .equals(data.getString(GHAST_ANGER_PLAYER_TAG));
    }

    public static EntityPlayer findClosestValidTarget(EntityGhast ghast, double maxDistance) {
        if (ghast == null || ghast.worldObj == null) return null;

        long worldTime = ghast.worldObj.getTotalWorldTime();
        double bestDistanceSq = maxDistance * maxDistance;
        EntityPlayer bestTarget = null;

        List<EntityPlayer> players = ghast.worldObj.playerEntities;
        for (EntityPlayer player : players) {
            if (player == null || !player.isEntityAlive() || player.capabilities.disableDamage) continue;

            double distanceSq = player.getDistanceSqToEntity(ghast);
            if (distanceSq > bestDistanceSq) continue;

            if (hasGhastUpgrade(player) && !isGhastAggressiveToPlayer(ghast, player, worldTime)) {
                continue;
            }

            bestDistanceSq = distanceSq;
            bestTarget = player;
        }

        return bestTarget;
    }
}
