package ruiseki.okbackpack.common.item.restock;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemRestockUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemRestockUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemRestockUpgradeConfig() {
        super(OKBackpack._instance, true, "restock_upgrade", null, config -> new ItemRestockUpgrade());
    }

}
