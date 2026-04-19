package ruiseki.okbackpack.common.item.travelers.spider;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

import ruiseki.okbackpack.api.wrapper.ISpiderUpgrade;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;

public final class SpiderUpgradeHelpers {

    private SpiderUpgradeHelpers() {}

    public static boolean shouldTreatAsOnLadder(EntityLivingBase entity) {
        if (entity == null || !entity.isCollidedHorizontally) {
            return false;
        }

        if (!(entity instanceof EntityPlayer player)) {
            return false;
        }

        return BackpackEntityHelpers.visitWornBackpacks(
            player,
            context -> hasEnabledSpiderUpgrade(
                context.wrapper()
                    .gatherCapabilityUpgrades(ISpiderUpgrade.class)
                    .values()));
    }

    public static boolean hasEnabledSpiderUpgrade(Iterable<? extends ISpiderUpgrade> upgrades) {
        if (upgrades == null) return false;

        for (ISpiderUpgrade upgrade : upgrades) {
            if (upgrade != null && upgrade.canClimbWalls()) {
                return true;
            }
        }

        return false;
    }
}
