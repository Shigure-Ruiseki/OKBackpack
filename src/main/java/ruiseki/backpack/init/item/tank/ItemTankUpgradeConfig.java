
package ruiseki.backpack.init.item.tank;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.tank.ItemTankUpgrade;
import ruiseki.okstoragecore.upgrades.tank.TankUpgradeConfig;

public class ItemTankUpgradeConfig extends ItemConfig {

    public static ItemTankUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.tank",
        comment = "Fluid capacity added per storage slot row.",
        configLocation = ConfigLocation.SERVER)
    public static int tankCapacityPerSlotRow = 16000;

    @ConfigurableProperty(
        category = "upgrade.tank",
        comment = "Fluid capacity multiplier per item stack.",
        configLocation = ConfigLocation.SERVER)
    public static double tankStackMultiplierRatio = 1.5D;

    @ConfigurableProperty(
        category = "upgrade.tank",
        comment = "Cooldown in ticks between automatic container operations.",
        configLocation = ConfigLocation.SERVER)
    public static int tankAutoFillDrainContainerCooldown = 10;

    @ConfigurableProperty(
        category = "upgrade.tank",
        comment = "Maximum fluid input and output per tick.",
        configLocation = ConfigLocation.SERVER)
    public static int tankMaxInputOutput = 1000;

    public ItemTankUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "tank_upgrade",
            null,
            config -> new ItemTankUpgrade(
                new TankUpgradeConfig(
                    () -> tankCapacityPerSlotRow,
                    () -> tankStackMultiplierRatio,
                    () -> tankAutoFillDrainContainerCooldown,
                    () -> tankMaxInputOutput),
                GeneralConfig.maxUpgradesPerStorageConfig)
                    .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.tank_upgrade.tooltip")));
    }
}
