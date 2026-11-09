package ruiseki.backpack.init.item.restock;

import ruiseki.backpack.upgrades.restock.ItemRestockUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvRestockUpgradeConfig extends ItemConfig {

    public static ItemAdvRestockUpgradeConfig _instance;

    public ItemAdvRestockUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_restock_upgrade",
            null,
            config -> new ItemRestockUpgrade(() -> 16, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
