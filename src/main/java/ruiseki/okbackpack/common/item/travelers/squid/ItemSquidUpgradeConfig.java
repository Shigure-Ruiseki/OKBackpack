package ruiseki.okbackpack.common.item.travelers.squid;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemSquidUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemSquidUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemSquidUpgradeConfig() {
        super(OKBackpack._instance, true, "squid_upgrade", null, config -> new ItemSquidUpgrade());
    }

}
