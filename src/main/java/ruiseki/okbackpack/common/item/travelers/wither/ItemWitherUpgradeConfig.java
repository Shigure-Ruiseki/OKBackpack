package ruiseki.okbackpack.common.item.travelers.wither;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemWitherUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemWitherUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemWitherUpgradeConfig() {
        super(OKBackpack._instance, true, "wither_upgrade", null, config -> new ItemWitherUpgrade());
    }

}
