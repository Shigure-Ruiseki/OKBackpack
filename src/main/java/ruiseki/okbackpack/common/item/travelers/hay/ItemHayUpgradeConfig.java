package ruiseki.okbackpack.common.item.travelers.hay;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemHayUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemHayUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemHayUpgradeConfig() {
        super(OKBackpack._instance, true, "hay_upgrade", null, config -> new ItemHayUpgrade());
    }

}
