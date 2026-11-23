package ruiseki.okbackpack.init.item.cooking;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.cooking.AutoCookingUpgradeConfig;
import ruiseki.okstoragecore.upgrades.cooking.ItemAutoSmeltingUpgrade;

public class ItemAutoSmeltingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAutoSmeltingUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.cooking.auto_smelting",
        comment = "Cooking speed multiplier.",
        configLocation = ConfigLocation.SERVER)
    public static double cookingSpeedMultiplier = 1d;

    @ConfigurableProperty(
        category = "upgrade.cooking.auto_smelting",
        comment = "Fuel efficiency multiplier.",
        configLocation = ConfigLocation.SERVER)
    public static double fuelEfficiencyMultiplier = 1d;

    /**
     * Make a new instance.
     */
    public ItemAutoSmeltingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "auto_smelting_upgrade",
            null,
            config -> new ItemAutoSmeltingUpgrade(
                new AutoCookingUpgradeConfig(
                    () -> cookingSpeedMultiplier,
                    () -> fuelEfficiencyMultiplier,
                    () -> 16,
                    () -> 4),
                GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                    builder -> builder.add(LangHelpers.localize("item.items.okbackpack.auto_smelting_upgrade.tooltip"))
                        .add(LangHelpers.localize("item.items.okbackpack.auto_smelting_upgrade.tooltip,1"))));
    }

}
