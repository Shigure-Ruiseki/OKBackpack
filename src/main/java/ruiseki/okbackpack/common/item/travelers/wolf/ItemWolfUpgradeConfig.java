package ruiseki.okbackpack.common.item.travelers.wolf;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemWolfUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemWolfUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemWolfUpgradeConfig() {
        super(OKBackpack._instance, true, "wolf_upgrade", null, config -> new ItemWolfUpgrade());
    }

}
