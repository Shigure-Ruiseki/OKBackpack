package ruiseki.backpack.init.item.feeding;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.feeding.ItemFeedingUpgrade;

public class ItemAdvFeedingUpgradeConfig extends ItemConfig {

    public static ItemAdvFeedingUpgradeConfig _instance;

    public ItemAdvFeedingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_feeding_upgrade",
            null,
            config -> new ItemFeedingUpgrade(() -> 16, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
