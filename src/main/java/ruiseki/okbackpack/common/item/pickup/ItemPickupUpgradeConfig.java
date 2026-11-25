package ruiseki.okbackpack.common.item.pickup;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemPickupUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemPickupUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemPickupUpgradeConfig() {
        super(OKBackpack._instance, true, "pickup_upgrade", null, config -> new ItemPickupUpgrade());
    }

}
