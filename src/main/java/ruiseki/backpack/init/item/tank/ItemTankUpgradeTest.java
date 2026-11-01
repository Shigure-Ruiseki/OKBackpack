
package ruiseki.backpack.init.item.tank;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.tank.ItemTankUpgrade;
import ruiseki.okstoragecore.upgrades.tank.TankUpgradeConfig;

public class ItemTankUpgradeTest extends ItemConfig {

    public static ItemTankUpgradeTest _instance;

    @ConfigurableProperty(
        category = "tank",
        comment = "Fluid capacity added per storage slot row.",
        configLocation = ConfigLocation.SERVER)
    public static int tankCapacityPerSlotRow = 16000;

    @ConfigurableProperty(
        category = "tank",
        comment = "Fluid capacity multiplier per item stack.",
        configLocation = ConfigLocation.SERVER)
    public static double tankStackMultiplierRatio = 1.5D;

    @ConfigurableProperty(
        category = "tank",
        comment = "Cooldown in ticks between automatic container operations.",
        configLocation = ConfigLocation.SERVER)
    public static int tankAutoFillDrainContainerCooldown = 10;

    @ConfigurableProperty(
        category = "tank",
        comment = "Maximum fluid input and output per tick.",
        configLocation = ConfigLocation.SERVER)
    public static int tankMaxInputOutput = 1000;

    public ItemTankUpgradeTest() {
        super(
            OKBackpack._instance,
            true,
            "tank_upgrade_test",
            null,
            config -> new ItemTankUpgrade(
                new TankUpgradeConfig(
                    () -> tankCapacityPerSlotRow,
                    () -> tankStackMultiplierRatio,
                    () -> tankAutoFillDrainContainerCooldown,
                    () -> tankMaxInputOutput),
                GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
