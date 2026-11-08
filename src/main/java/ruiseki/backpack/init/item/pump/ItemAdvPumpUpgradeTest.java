package ruiseki.backpack.init.item.pump;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.pump.ItemPumpUpgrade;
import ruiseki.okstoragecore.upgrades.pump.PumpUpgradeConfig;

public class ItemAdvPumpUpgradeTest extends ItemConfig {

    public static ItemAdvPumpUpgradeTest _instance;

    @ConfigurableProperty(
        category = "upgrade.pump",
        comment = "Maximum Fluid Input/Output (mB)",
        configLocation = ConfigLocation.SERVER)
    public static int maxInputOutput = 1000;

    @ConfigurableProperty(
        category = "upgrade.pump",
        comment = "Stack Multiplier Ratio",
        configLocation = ConfigLocation.SERVER)
    public static double stackMultiplierRatio = 1.0;

    @ConfigurableProperty(
        category = "upgrade.pump",
        comment = "Interact with Fluid Containers in Player Hands by Default",
        configLocation = ConfigLocation.SERVER)
    public static boolean interactWithHandDefault = false;

    @ConfigurableProperty(
        category = "upgrade.pump",
        comment = "Interact with Fluid Blocks in World by Default",
        configLocation = ConfigLocation.SERVER)
    public static boolean interactWithWorldDefault = false;

    public ItemAdvPumpUpgradeTest() {
        super(
            OKBackpack._instance,
            true,
            "adv_pump_upgrade_test",
            null,
            config -> new ItemPumpUpgrade(
                new PumpUpgradeConfig(() -> maxInputOutput, () -> stackMultiplierRatio, () -> 16),
                () -> interactWithHandDefault,
                () -> interactWithWorldDefault,
                GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
