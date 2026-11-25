package ruiseki.okbackpack.common.item.travelers.cow;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemCowUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemCowUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemCowUpgradeConfig() {
        super(OKBackpack._instance, true, "cow_upgrade", null, config -> new ItemCowUpgrade());
    }

}
