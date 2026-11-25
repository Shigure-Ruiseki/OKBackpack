package ruiseki.okbackpack.common.helpers;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.api.wrapper.IArcaneCraftingUpgrade;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;

public final class UpgradeFeatureHelper {

    private UpgradeFeatureHelper() {}

    public static boolean areTravelersUpgradesEnabled() {
        return GeneralConfig.enableTravelersUpgrades;
    }

    public static boolean isArcaneCraftingUpgradeEnabled() {
        return GeneralConfig.enableArcaneCraftingUpgrade;
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
