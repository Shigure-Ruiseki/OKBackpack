package ruiseki.backpack.init.item.restock;

import ruiseki.backpack.upgrades.restock.ItemRestockUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemRestockUpgradeConfig extends ItemConfig {

    public static ItemRestockUpgradeConfig _instance;

    public ItemRestockUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "restock_upgrade",
            null,
            config -> new ItemRestockUpgrade(() -> 9, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
