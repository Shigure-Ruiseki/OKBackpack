package ruiseki.backpack.init.item.restock;

import ruiseki.backpack.upgrades.restock.ItemRestockUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvRestockUpgradeTest extends ItemConfig {

    public static ItemAdvRestockUpgradeTest _instance;

    public ItemAdvRestockUpgradeTest() {
        super(
            OKBackpack._instance,
            true,
            "adv_restock_upgrade_test",
            null,
            config -> new ItemRestockUpgrade(() -> 16, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
