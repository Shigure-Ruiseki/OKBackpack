package ruiseki.okbackpack.common.item.pump;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemPumpUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemPumpUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemPumpUpgradeConfig() {
        super(OKBackpack._instance, true, "pump_upgrade", null, config -> new ItemPumpUpgrade());
    }

}
