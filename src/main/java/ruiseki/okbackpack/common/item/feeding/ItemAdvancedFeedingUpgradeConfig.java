package ruiseki.okbackpack.common.item.feeding;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvancedFeedingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvancedFeedingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvancedFeedingUpgradeConfig() {
        super(OKBackpack._instance, true, "advanced_feeding_upgrade", null, config -> new ItemAdvancedFeedingUpgrade());
    }

}
