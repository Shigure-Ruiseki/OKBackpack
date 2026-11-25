package ruiseki.okbackpack.common.item.deposit;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemDepositUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemDepositUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemDepositUpgradeConfig() {
        super(OKBackpack._instance, true, "deposit_upgrade", null, config -> new ItemDepositUpgrade());
    }

}
