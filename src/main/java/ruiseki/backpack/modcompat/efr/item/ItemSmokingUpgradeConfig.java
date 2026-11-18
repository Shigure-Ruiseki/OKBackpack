package ruiseki.backpack.modcompat.efr.item;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.cooking.CookingUpgradeConfig;
import ruiseki.okstoragecore.upgrades.cooking.ItemSmokingUpgrade;

public class ItemSmokingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemSmokingUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.cooking.smoking",
        comment = "Cooking speed multiplier.",
        configLocation = ConfigLocation.SERVER)
    public static double cookingSpeedMultiplier = 1d;

    @ConfigurableProperty(
        category = "upgrade.cooking.smoking",
        comment = "Fuel efficiency multiplier.",
        configLocation = ConfigLocation.SERVER)
    public static double fuelEfficiencyMultiplier = 1d;

    /**
     * Make a new instance.
     */
    public ItemSmokingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "smoking_upgrade",
            null,
            config -> new ItemSmokingUpgrade(
                new CookingUpgradeConfig(() -> cookingSpeedMultiplier, () -> fuelEfficiencyMultiplier),
                GeneralConfig.maxUpgradesPerStorageConfig)
                    .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.smoking_upgrade.tooltip")));
    }

}
