package ruiseki.okbackpack.common.item.voiding;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvancedVoidUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvancedVoidUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvancedVoidUpgradeConfig() {
        super(OKBackpack._instance, true, "advanced_void_upgrade", null, config -> new ItemAdvancedVoidUpgrade());
    }

}
