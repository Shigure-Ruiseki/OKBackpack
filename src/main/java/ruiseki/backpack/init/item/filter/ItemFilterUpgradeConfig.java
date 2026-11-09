package ruiseki.backpack.init.item.filter;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.filter.ItemFilterUpgrade;

public class ItemFilterUpgradeConfig extends ItemConfig {

    public static ItemFilterUpgradeConfig _instance;

    public ItemFilterUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "filter_upgrade",
            null,
            config -> new ItemFilterUpgrade(() -> 9, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
