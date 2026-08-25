package ruiseki.okbackpack.common.item.smelter;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAutoSmokingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAutoSmokingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAutoSmokingUpgradeConfig() {
        super(OKBackpack._instance, true, "auto_smoking_upgrade", null, config -> new ItemAutoSmokingUpgrade());
    }

}
