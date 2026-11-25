package ruiseki.okbackpack.common.item.refill;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvancedRefillUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvancedRefillUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvancedRefillUpgradeConfig() {
        super(OKBackpack._instance, true, "advanced_refill_upgrade", null, config -> new ItemAdvancedRefillUpgrade());
    }

}
