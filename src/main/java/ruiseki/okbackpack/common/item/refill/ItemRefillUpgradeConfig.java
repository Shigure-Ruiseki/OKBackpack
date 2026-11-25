package ruiseki.okbackpack.common.item.refill;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemRefillUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemRefillUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemRefillUpgradeConfig() {
        super(OKBackpack._instance, true, "refill_upgrade", null, config -> new ItemRefillUpgrade());
    }

}
