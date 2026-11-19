package ruiseki.backpack.init.item.cooking;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.cooking.CookingUpgradeConfig;
import ruiseki.okstoragecore.upgrades.cooking.ItemSmeltingUpgrade;

public class ItemSmeltingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemSmeltingUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.cooking.smelting",
        comment = "Cooking speed multiplier.",
        configLocation = ConfigLocation.SERVER)
    public static double cookingSpeedMultiplier = 1d;

    @ConfigurableProperty(
        category = "upgrade.cooking.smelting",
        comment = "Fuel efficiency multiplier.",
        configLocation = ConfigLocation.SERVER)
    public static double fuelEfficiencyMultiplier = 1d;

    /**
     * Make a new instance.
     */
    public ItemSmeltingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "smelting_upgrade",
            null,
            config -> new ItemSmeltingUpgrade(
                new CookingUpgradeConfig(() -> cookingSpeedMultiplier, () -> fuelEfficiencyMultiplier),
                GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                    builder -> builder.add(LangHelpers.localize("item.items.okbackpack.smelting_upgrade.tooltip"))));
    }

}
