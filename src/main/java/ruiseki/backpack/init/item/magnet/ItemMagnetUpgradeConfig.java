package ruiseki.backpack.init.item.magnet;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.magnet.ItemMagnetUpgrade;

public class ItemMagnetUpgradeConfig extends ItemConfig {

    public static ItemMagnetUpgradeConfig _instance;

    @ConfigurableProperty(category = "upgrade.magnet", comment = "Magnet Range", configLocation = ConfigLocation.SERVER)
    public static int basicMagnetRange = 2;

    public ItemMagnetUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "magnet_upgrade",
            null,
            config -> new ItemMagnetUpgrade(() -> basicMagnetRange, () -> 16, GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.magnet_upgrade.tooltip")));
    }
}
