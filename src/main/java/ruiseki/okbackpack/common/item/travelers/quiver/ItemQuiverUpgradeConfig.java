package ruiseki.okbackpack.common.item.travelers.quiver;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemQuiverUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemQuiverUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemQuiverUpgradeConfig() {
        super(OKBackpack._instance, true, "quiver_upgrade", null, config -> new ItemQuiverUpgrade());
    }

}
