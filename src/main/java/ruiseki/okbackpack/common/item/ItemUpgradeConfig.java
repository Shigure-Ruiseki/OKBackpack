package ruiseki.okbackpack.common.item;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemUpgradeConfig() {
        super(OKBackpack._instance, true, "upgrade_base", null, config -> new ItemUpgrade<>());
    }

}
