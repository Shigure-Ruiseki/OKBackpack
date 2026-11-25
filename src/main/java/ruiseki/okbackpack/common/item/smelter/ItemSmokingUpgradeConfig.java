package ruiseki.okbackpack.common.item.smelter;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemSmokingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemSmokingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemSmokingUpgradeConfig() {
        super(OKBackpack._instance, true, "smoking_upgrade", null, config -> new ItemSmokingUpgrade());
    }

}
