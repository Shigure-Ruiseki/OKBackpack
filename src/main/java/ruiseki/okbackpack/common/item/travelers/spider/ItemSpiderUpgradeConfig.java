package ruiseki.okbackpack.common.item.travelers.spider;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemSpiderUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemSpiderUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemSpiderUpgradeConfig() {
        super(OKBackpack._instance, true, "spider_upgrade", null, config -> new ItemSpiderUpgrade());
    }

}
