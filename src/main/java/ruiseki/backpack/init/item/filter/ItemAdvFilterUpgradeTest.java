package ruiseki.backpack.init.item.filter;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.filter.ItemFilterUpgrade;

public class ItemAdvFilterUpgradeTest extends ItemConfig {

    public static ItemAdvFilterUpgradeTest _instance;

    public ItemAdvFilterUpgradeTest() {
        super(
            OKBackpack._instance,
            true,
            "adv_filter_upgrade_test",
            null,
            config -> new ItemFilterUpgrade(() -> 16, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
