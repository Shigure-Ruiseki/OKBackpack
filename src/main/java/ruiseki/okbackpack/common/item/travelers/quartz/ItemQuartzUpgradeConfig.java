package ruiseki.okbackpack.common.item.travelers.quartz;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemQuartzUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemQuartzUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemQuartzUpgradeConfig() {
        super(OKBackpack._instance, true, "quartz_upgrade", null, config -> new ItemQuartzUpgrade());
    }

}
