package ruiseki.backpack.init.item.feeding;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.feeding.ItemFeedingUpgrade;

public class ItemAdvFeedingUpgradeTest extends ItemConfig {

    public static ItemAdvFeedingUpgradeTest _instance;

    public ItemAdvFeedingUpgradeTest() {
        super(
            OKBackpack._instance,
            true,
            "adv_feeding_upgrade_test",
            null,
            config -> new ItemFeedingUpgrade(() -> 16, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
