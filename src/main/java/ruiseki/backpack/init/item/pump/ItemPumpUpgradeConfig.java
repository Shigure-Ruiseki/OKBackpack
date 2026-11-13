package ruiseki.backpack.init.item.pump;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.pump.ItemPumpUpgrade;
import ruiseki.okstoragecore.upgrades.pump.PumpUpgradeConfig;

public class ItemPumpUpgradeConfig extends ItemConfig {

    public static ItemPumpUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.pump",
        comment = "Maximum Fluid Input/Output (mB)",
        configLocation = ConfigLocation.SERVER)
    public static int basicMaxInputOutput = 1000;

    @ConfigurableProperty(
        category = "upgrade.pump",
        comment = "Stack Multiplier Ratio",
        configLocation = ConfigLocation.SERVER)
    public static double basicStackMultiplierRatio = 1.0;

    @ConfigurableProperty(
        category = "upgrade.pump",
        comment = "Interact with Fluid Containers in Player Hands by Default",
        configLocation = ConfigLocation.SERVER)
    public static boolean basicInteractWithHandDefault = false;

    @ConfigurableProperty(
        category = "upgrade.pump",
        comment = "Interact with Fluid Blocks in World by Default",
        configLocation = ConfigLocation.SERVER)
    public static boolean basicInteractWithWorldDefault = false;

    public ItemPumpUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "pump_upgrade",
            null,
            config -> new ItemPumpUpgrade(
                new PumpUpgradeConfig(() -> basicMaxInputOutput, () -> basicStackMultiplierRatio, () -> 16),
                () -> basicInteractWithHandDefault,
                () -> basicInteractWithWorldDefault,
                GeneralConfig.maxUpgradesPerStorageConfig)
                    .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.pump_upgrade.tooltip")));
    }
}
