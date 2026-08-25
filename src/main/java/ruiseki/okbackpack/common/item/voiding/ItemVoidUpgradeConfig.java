package ruiseki.okbackpack.common.item.voiding;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemVoidUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemVoidUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemVoidUpgradeConfig() {
        super(OKBackpack._instance, true, "void_upgrade", null, config -> new ItemVoidUpgrade());
    }

}
