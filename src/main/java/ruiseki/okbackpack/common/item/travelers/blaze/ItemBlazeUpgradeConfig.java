package ruiseki.okbackpack.common.item.travelers.blaze;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemBlazeUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemBlazeUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemBlazeUpgradeConfig() {
        super(OKBackpack._instance, true, "blaze_upgrade", null, config -> new ItemBlazeUpgrade());
    }

}
