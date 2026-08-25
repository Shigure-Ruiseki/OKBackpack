package ruiseki.okbackpack.common.item.travelers.slime;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemSlimeUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemSlimeUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemSlimeUpgradeConfig() {
        super(OKBackpack._instance, true, "slime_upgrade", null, config -> new ItemSlimeUpgrade());
    }

}
