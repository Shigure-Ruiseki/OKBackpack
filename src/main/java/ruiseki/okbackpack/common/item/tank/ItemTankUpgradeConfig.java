package ruiseki.okbackpack.common.item.tank;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemTankUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemTankUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemTankUpgradeConfig() {
        super(OKBackpack._instance, true, "tank_upgrade", null, config -> new ItemTankUpgrade());
    }

}
