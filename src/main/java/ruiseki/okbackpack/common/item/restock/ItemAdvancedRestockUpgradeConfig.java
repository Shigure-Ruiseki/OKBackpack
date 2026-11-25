package ruiseki.okbackpack.common.item.restock;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvancedRestockUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvancedRestockUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvancedRestockUpgradeConfig() {
        super(OKBackpack._instance, true, "advanced_restock_upgrade", null, config -> new ItemAdvancedRestockUpgrade());
    }

}
