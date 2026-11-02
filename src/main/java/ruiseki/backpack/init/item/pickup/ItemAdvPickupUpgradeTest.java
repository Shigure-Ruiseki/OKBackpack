package ruiseki.backpack.init.item.pickup;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.pickup.ItemPickupUpgrade;

public class ItemAdvPickupUpgradeTest extends ItemConfig {

    public static ItemAdvPickupUpgradeTest _instance;

    public ItemAdvPickupUpgradeTest() {
        super(
            OKBackpack._instance,
            true,
            "adv_pickup_upgrade_test",
            null,
            config -> new ItemPickupUpgrade(() -> 16, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
