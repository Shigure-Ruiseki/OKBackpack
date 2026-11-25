package ruiseki.okbackpack.common.item.pickup;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvancedPickupUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvancedPickupUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvancedPickupUpgradeConfig() {
        super(OKBackpack._instance, true, "advanced_pickup_upgrade", null, config -> new ItemAdvancedPickupUpgrade());
    }

}
