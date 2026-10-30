package ruiseki.backpack.init.item.battery;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.battery.BatteryUpgradeConfig;
import ruiseki.okstoragecore.upgrades.battery.ItemBatteryUpgrade;

public class ItemBatteryUpgradeTest extends ItemConfig {

    public static ItemBatteryUpgradeTest _instance;

    @ConfigurableProperty(
        category = "battery",
        comment = "Energy capacity added per storage slot row.",
        configLocation = ConfigLocation.SERVER)
    public static int batteryEnergyPerSlotRow = 10000;

    @ConfigurableProperty(
        category = "battery",
        comment = "Energy capacity multiplier per item stack.",
        configLocation = ConfigLocation.SERVER)
    public static double batteryStackMultiplierRatio = 1.5D;

    @ConfigurableProperty(
        category = "battery",
        comment = "Maximum energy input and output per tick.",
        configLocation = ConfigLocation.SERVER)
    public static int batteryMaxInputOutput = 1000;

    public ItemBatteryUpgradeTest() {
        super(
            OKBackpack._instance,
            true,
            "battery_upgrade_test",
            null,
            config -> new ItemBatteryUpgrade(
                new BatteryUpgradeConfig(
                    () -> batteryEnergyPerSlotRow,
                    () -> batteryStackMultiplierRatio,
                    () -> batteryMaxInputOutput),
                GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
