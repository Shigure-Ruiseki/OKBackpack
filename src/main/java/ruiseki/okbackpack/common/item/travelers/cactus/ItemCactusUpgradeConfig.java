package ruiseki.okbackpack.common.item.travelers.cactus;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemCactusUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemCactusUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemCactusUpgradeConfig() {
        super(OKBackpack._instance, true, "cactus_upgrade", null, config -> new ItemCactusUpgrade());
    }

}
