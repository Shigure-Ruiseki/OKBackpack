package ruiseki.okbackpack.common.item.smelter;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAutoSmeltingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAutoSmeltingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAutoSmeltingUpgradeConfig() {
        super(OKBackpack._instance, true, "auto_smelting_upgrade", null, config -> new ItemAutoSmeltingUpgrade());
    }

}
