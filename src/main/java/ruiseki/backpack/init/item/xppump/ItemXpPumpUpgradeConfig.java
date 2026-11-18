package ruiseki.backpack.init.item.xppump;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.xppump.ItemXpPumpUpgrade;
import ruiseki.okstoragecore.upgrades.xppump.XpPumpUpgradeConfig;

public class ItemXpPumpUpgradeConfig extends ItemConfig {

    public static ItemXpPumpUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.xp_pump",
        comment = "Enable Mending functionality",
        configLocation = ConfigLocation.SERVER)
    public static boolean mendingOn = true;

    @ConfigurableProperty(
        category = "upgrade.xp_pump",
        comment = "Maximum XP points consumed per item when mending",
        configLocation = ConfigLocation.SERVER)
    public static int maxXpPointsPerMending = 10;

    public ItemXpPumpUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "xp_pump_upgrade",
            null,
            config -> new ItemXpPumpUpgrade(
                new XpPumpUpgradeConfig(() -> maxXpPointsPerMending, () -> mendingOn),
                GeneralConfig.maxUpgradesPerStorageConfig)
                    .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.xp_pump_upgrade.tooltip")));
    }
}
