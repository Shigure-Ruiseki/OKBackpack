package ruiseki.okbackpack.common.item.deposit;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvancedDepositUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvancedDepositUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvancedDepositUpgradeConfig() {
        super(OKBackpack._instance, true, "advanced_deposit_upgrade", null, config -> new ItemAdvancedDepositUpgrade());
    }

}
