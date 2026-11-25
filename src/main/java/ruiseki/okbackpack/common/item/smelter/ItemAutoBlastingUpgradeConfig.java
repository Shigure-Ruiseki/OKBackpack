package ruiseki.okbackpack.common.item.smelter;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAutoBlastingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAutoBlastingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAutoBlastingUpgradeConfig() {
        super(OKBackpack._instance, true, "auto_blasting_upgrade", null, config -> new ItemAutoBlastingUpgrade());
    }

}
