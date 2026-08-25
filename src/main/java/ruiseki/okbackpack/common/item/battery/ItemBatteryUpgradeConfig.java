package ruiseki.okbackpack.common.item.battery;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemBatteryUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemBatteryUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemBatteryUpgradeConfig() {
        super(OKBackpack._instance, true, "battery_upgrade", null, config -> new ItemBatteryUpgrade());
    }

}
