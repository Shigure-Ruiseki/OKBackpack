package ruiseki.okbackpack.common.item.travelers.dragon;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemDragonUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemDragonUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemDragonUpgradeConfig() {
        super(OKBackpack._instance, true, "dragon_upgrade", null, config -> new ItemDragonUpgrade());
    }

}
