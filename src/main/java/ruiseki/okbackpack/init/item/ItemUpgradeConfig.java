package ruiseki.okbackpack.init.item;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.item.ItemBase;

public class ItemUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemUpgradeConfig() {
        super(OKBackpack._instance, true, "upgrade_base", null, config -> new ItemBase());
    }

}
