package ruiseki.okbackpack.common.item.travelers.lapis;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemLapisUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemLapisUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemLapisUpgradeConfig() {
        super(OKBackpack._instance, true, "lapis_upgrade", null, config -> new ItemLapisUpgrade());
    }

}
