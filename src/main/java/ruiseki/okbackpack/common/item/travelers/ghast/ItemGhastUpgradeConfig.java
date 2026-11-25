package ruiseki.okbackpack.common.item.travelers.ghast;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemGhastUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemGhastUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemGhastUpgradeConfig() {
        super(OKBackpack._instance, true, "ghast_upgrade", null, config -> new ItemGhastUpgrade());
    }

}
