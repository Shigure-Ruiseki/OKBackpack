package ruiseki.backpack.init.item.magnet;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.magnet.ItemMagnetUpgrade;

public class ItemAdvMagnetUpgradeTest extends ItemConfig {

    public static ItemAdvMagnetUpgradeTest _instance;

    @ConfigurableProperty(category = "upgrade.magnet", comment = "Magnet Range", configLocation = ConfigLocation.SERVER)
    public static int magnetRange = 5;

    public ItemAdvMagnetUpgradeTest() {
        super(
            OKBackpack._instance,
            true,
            "adv_magnet_upgrade_test",
            null,
            config -> new ItemMagnetUpgrade(() -> magnetRange, () -> 16, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
