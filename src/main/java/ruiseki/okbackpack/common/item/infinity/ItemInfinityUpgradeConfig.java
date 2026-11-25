package ruiseki.okbackpack.common.item.infinity;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemInfinityUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemInfinityUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemInfinityUpgradeConfig() {
        super(OKBackpack._instance, true, "infinity_upgrade", null, config -> new ItemInfinityUpgrade());
    }

}
