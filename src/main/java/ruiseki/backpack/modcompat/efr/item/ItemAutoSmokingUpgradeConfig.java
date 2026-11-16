package ruiseki.backpack.modcompat.efr.item;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.cooking.AutoCookingUpgradeConfig;
import ruiseki.okstoragecore.upgrades.cooking.ItemAutoSmokingUpgrade;

public class ItemAutoSmokingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAutoSmokingUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.cooking.auto_smoking",
        comment = "Cooking speed multiplier.",
        configLocation = ConfigLocation.SERVER)
    public static double cookingSpeedMultiplier = 1d;

    @ConfigurableProperty(
        category = "upgrade.cooking.auto_smoking",
        comment = "Fuel efficiency multiplier.",
        configLocation = ConfigLocation.SERVER)
    public static double fuelEfficiencyMultiplier = 1d;

    /**
     * Make a new instance.
     */
    public ItemAutoSmokingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "auto_smoking_upgrade",
            null,
            config -> new ItemAutoSmokingUpgrade(
                new AutoCookingUpgradeConfig(
                    () -> cookingSpeedMultiplier,
                    () -> fuelEfficiencyMultiplier,
                    () -> 16,
                    () -> 4),
                GeneralConfig.maxUpgradesPerStorageConfig));
    }

}
