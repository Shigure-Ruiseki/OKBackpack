package ruiseki.okbackpack.common.item.filter;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemFilterUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemFilterUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemFilterUpgradeConfig() {
        super(OKBackpack._instance, true, "filter_upgrade", null, config -> new ItemFilterUpgrade());
    }

}
