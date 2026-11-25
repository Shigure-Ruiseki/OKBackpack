package ruiseki.okbackpack.common.item.inception;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemInceptionUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemInceptionUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemInceptionUpgradeConfig() {
        super(OKBackpack._instance, true, "inception_upgrade", null, config -> new ItemInceptionUpgrade());
    }

}
