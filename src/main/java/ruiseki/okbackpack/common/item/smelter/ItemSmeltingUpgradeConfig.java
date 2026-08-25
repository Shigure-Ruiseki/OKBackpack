package ruiseki.okbackpack.common.item.smelter;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemSmeltingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemSmeltingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemSmeltingUpgradeConfig() {
        super(OKBackpack._instance, true, "smelting_upgrade", null, config -> new ItemSmeltingUpgrade());
    }

}
