package ruiseki.okbackpack.common.item.travelers.creeper;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemCreeperUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemCreeperUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemCreeperUpgradeConfig() {
        super(OKBackpack._instance, true, "creeper_upgrade", null, config -> new ItemCreeperUpgrade());
    }

}
