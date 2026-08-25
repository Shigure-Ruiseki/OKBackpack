package ruiseki.okbackpack.common.item.travelers.cake;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemCakeUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemCakeUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemCakeUpgradeConfig() {
        super(OKBackpack._instance, true, "cake_upgrade", null, config -> new ItemCakeUpgrade());
    }

}
