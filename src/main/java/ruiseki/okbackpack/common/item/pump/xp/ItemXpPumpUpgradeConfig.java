package ruiseki.okbackpack.common.item.pump.xp;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemXpPumpUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemXpPumpUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemXpPumpUpgradeConfig() {
        super(OKBackpack._instance, true, "xp_pump_upgrade", null, config -> new ItemXpPumpUpgrade());
    }

}
