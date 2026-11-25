package ruiseki.okbackpack.common.item.feeding;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemFeedingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemFeedingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemFeedingUpgradeConfig() {
        super(OKBackpack._instance, true, "feeding_upgrade", null, config -> new ItemFeedingUpgrade());
    }

}
