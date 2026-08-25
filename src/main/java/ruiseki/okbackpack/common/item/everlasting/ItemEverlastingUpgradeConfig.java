package ruiseki.okbackpack.common.item.everlasting;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemEverlastingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemEverlastingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemEverlastingUpgradeConfig() {
        super(OKBackpack._instance, true, "everlasting_upgrade", null, config -> new ItemEverlastingUpgrade());
    }

}
