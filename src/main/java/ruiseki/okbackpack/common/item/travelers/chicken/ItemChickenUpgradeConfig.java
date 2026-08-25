package ruiseki.okbackpack.common.item.travelers.chicken;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemChickenUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemChickenUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemChickenUpgradeConfig() {
        super(OKBackpack._instance, true, "chicken_upgrade", null, config -> new ItemChickenUpgrade());
    }

}
