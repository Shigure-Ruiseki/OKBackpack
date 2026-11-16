package ruiseki.backpack.modcompat.efr.item;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.cooking.AutoCookingUpgradeConfig;
import ruiseki.okstoragecore.upgrades.cooking.ItemAutoBlastingUpgrade;

public class ItemAutoBlastingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAutoBlastingUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.cooking.auto_blasting",
        comment = "Cooking speed multiplier.",
        configLocation = ConfigLocation.SERVER)
    public static double cookingSpeedMultiplier = 1d;

    @ConfigurableProperty(
        category = "upgrade.cooking.auto_blasting",
        comment = "Fuel efficiency multiplier.",
        configLocation = ConfigLocation.SERVER)
    public static double fuelEfficiencyMultiplier = 1d;

    /**
     * Make a new instance.
     */
    public ItemAutoBlastingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "auto_blasting_upgrade",
            null,
            config -> new ItemAutoBlastingUpgrade(
                new AutoCookingUpgradeConfig(
                    () -> cookingSpeedMultiplier,
                    () -> fuelEfficiencyMultiplier,
                    () -> 16,
                    () -> 4),
                GeneralConfig.maxUpgradesPerStorageConfig));
    }

}
