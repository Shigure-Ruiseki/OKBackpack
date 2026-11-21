package ruiseki.backpack.upgrades.traverler.ghast;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import ruiseki.backpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.backpack.helpers.PlayerInventoryProvider;
import ruiseki.okcore.helper.CapabilityHelpers;

public final class GhastUpgradeHelpers {

    private static final int GHAST_RETALIATE_TICKS = 200;
    private static final String GHAST_ANGER_PLAYER_TAG = "OKBGhastAngerPlayer";
    private static final String GHAST_ANGER_UNTIL_TAG = "OKBGhastAngerUntil";

    private GhastUpgradeHelpers() {}

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

            if (hasUpgrade(player) && !isGhastAggressiveToPlayer(ghast, player, worldTime)) {
                continue;
            }

            bestDistanceSq = distanceSq;
            bestTarget = player;
        }

        return bestTarget;
    }

    public static boolean hasUpgrade(EntityPlayer player) {
        if (player == null || player.worldObj == null) return false;

        AtomicBoolean found = new AtomicBoolean(false);
        PlayerInventoryProvider.get()
            .runOnBackpacks(player, (backpack, handlerName, identifier, slot) -> {
                boolean hasUpgrade = CapabilityHelpers
                    .getCapability(backpack, CapabilityBackpackWrapperConfig.CAPABILITY)
                    .map(
                        wrapper -> wrapper.getUpgradeHandler()
                            .hasUpgrade(ItemGhastUpgrade.TYPE))
                    .orElse(false);
                if (hasUpgrade) {
                    found.set(true);
                }
                return hasUpgrade;
            });
        return found.get();
    }
}
