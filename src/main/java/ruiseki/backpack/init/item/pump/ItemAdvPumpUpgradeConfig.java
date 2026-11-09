package ruiseki.backpack.init.item.pump;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.pump.ItemPumpUpgrade;
import ruiseki.okstoragecore.upgrades.pump.PumpUpgradeConfig;

public class ItemAdvPumpUpgradeConfig extends ItemConfig {

    public static ItemAdvPumpUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.pump",
        comment = "Maximum Fluid Input/Output (mB)",
        configLocation = ConfigLocation.SERVER)
    public static int advMaxInputOutput = 1000;

    @ConfigurableProperty(
        category = "upgrade.pump",
        comment = "Stack Multiplier Ratio",
        configLocation = ConfigLocation.SERVER)
    public static double advStackMultiplierRatio = 1.0;

    @ConfigurableProperty(
        category = "upgrade.pump",
        comment = "Interact with Fluid Containers in Player Hands by Default",
        configLocation = ConfigLocation.SERVER)
    public static boolean advInteractWithHandDefault = false;

    @ConfigurableProperty(
        category = "upgrade.pump",
        comment = "Interact with Fluid Blocks in World by Default",
        configLocation = ConfigLocation.SERVER)
    public static boolean advInteractWithWorldDefault = false;

    public ItemAdvPumpUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_pump_upgrade",
            null,
            config -> new ItemPumpUpgrade(
                new PumpUpgradeConfig(() -> advMaxInputOutput, () -> advStackMultiplierRatio, () -> 16),
                () -> advInteractWithHandDefault,
                () -> advInteractWithWorldDefault,
                GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
