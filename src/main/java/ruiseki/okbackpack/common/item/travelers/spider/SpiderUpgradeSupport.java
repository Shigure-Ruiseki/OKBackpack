package ruiseki.okbackpack.common.item.travelers.spider;

import net.minecraft.entity.EntityLivingBase;

import ruiseki.okbackpack.api.wrapper.ISpiderUpgrade;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelper;

public final class SpiderUpgradeSupport {

    private SpiderUpgradeSupport() {}

    public static boolean shouldTreatAsOnLadder(EntityLivingBase entity) {
        if (entity == null || !entity.isCollidedHorizontally) {
            return false;
        }

        return BackpackEntityHelper.visitBackpacks(
            entity,
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
