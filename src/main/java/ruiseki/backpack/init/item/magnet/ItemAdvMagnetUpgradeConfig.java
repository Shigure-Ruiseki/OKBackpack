package ruiseki.backpack.init.item.magnet;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.magnet.ItemMagnetUpgrade;

public class ItemAdvMagnetUpgradeConfig extends ItemConfig {

    public static ItemAdvMagnetUpgradeConfig _instance;

    @ConfigurableProperty(category = "upgrade.magnet", comment = "Magnet Range", configLocation = ConfigLocation.SERVER)
    public static int AdvMagnetRange = 5;

    public ItemAdvMagnetUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_magnet_upgrade",
            null,
            config -> new ItemMagnetUpgrade(() -> AdvMagnetRange, () -> 16, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
