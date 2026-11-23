package ruiseki.okbackpack.modcompat.efr.item;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.cooking.CookingUpgradeConfig;
import ruiseki.okstoragecore.upgrades.cooking.ItemBlastingUpgrade;

public class ItemBlastingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemBlastingUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.cooking.blasting",
        comment = "Cooking speed multiplier.",
        configLocation = ConfigLocation.SERVER)
    public static double cookingSpeedMultiplier = 1d;

    @ConfigurableProperty(
        category = "upgrade.cooking.blasting",
        comment = "Fuel efficiency multiplier.",
        configLocation = ConfigLocation.SERVER)
    public static double fuelEfficiencyMultiplier = 1d;

    /**
     * Make a new instance.
     */
    public ItemBlastingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "blasting_upgrade",
            null,
            config -> new ItemBlastingUpgrade(
                new CookingUpgradeConfig(() -> cookingSpeedMultiplier, () -> fuelEfficiencyMultiplier),
                GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                    builder -> builder.add(LangHelpers.localize("item.items.okbackpack.blasting_upgrade.tooltip"))));
    }

}
