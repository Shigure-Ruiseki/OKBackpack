package ruiseki.okbackpack.common.item.pump;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvancedPumpUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvancedPumpUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvancedPumpUpgradeConfig() {
        super(OKBackpack._instance, true, "advanced_pump_upgrade", null, config -> new ItemAdvancedPumpUpgrade());
    }

}
