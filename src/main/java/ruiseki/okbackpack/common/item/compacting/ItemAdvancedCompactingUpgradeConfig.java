package ruiseki.okbackpack.common.item.compacting;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvancedCompactingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvancedCompactingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvancedCompactingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_compacting_upgrade",
            null,
            config -> new ItemAdvancedCompactingUpgrade());
    }

}
