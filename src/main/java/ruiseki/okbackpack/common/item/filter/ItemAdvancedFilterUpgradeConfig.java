package ruiseki.okbackpack.common.item.filter;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvancedFilterUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvancedFilterUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvancedFilterUpgradeConfig() {
        super(OKBackpack._instance, true, "advanced_filter_upgrade", null, config -> new ItemAdvancedFilterUpgrade());
    }

}
