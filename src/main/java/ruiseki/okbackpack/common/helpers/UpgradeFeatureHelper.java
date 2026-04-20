package ruiseki.okbackpack.common.helpers;

import ruiseki.okbackpack.api.wrapper.IArcaneCraftingUpgrade;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.config.ModConfig;

public final class UpgradeFeatureHelper {

    private UpgradeFeatureHelper() {}

    public static boolean areTravelersUpgradesEnabled() {
        return ModConfig.enableTravelersUpgrades;
    }

    public static boolean isArcaneCraftingUpgradeEnabled() {
        return ModConfig.enableArcaneCraftingUpgrade;
    }

    public static boolean isUpgradeRuntimeEnabled(IUpgradeWrapper wrapper) {
        if (wrapper instanceof ITravelersUpgrade) {
            return areTravelersUpgradesEnabled();
        }
        if (wrapper instanceof IArcaneCraftingUpgrade) {
            return isArcaneCraftingUpgradeEnabled();
        }
        return true;
    }
}
