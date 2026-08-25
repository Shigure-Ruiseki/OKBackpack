package ruiseki.okbackpack.common.item.smelter;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemBlastingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemBlastingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemBlastingUpgradeConfig() {
        super(OKBackpack._instance, true, "blasting_upgrade", null, config -> new ItemBlastingUpgrade());
    }

}
