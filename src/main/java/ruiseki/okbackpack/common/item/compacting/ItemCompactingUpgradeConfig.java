package ruiseki.okbackpack.common.item.compacting;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemCompactingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemCompactingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemCompactingUpgradeConfig() {
        super(OKBackpack._instance, true, "compacting_upgrade", null, config -> new ItemCompactingUpgrade());
    }

}
