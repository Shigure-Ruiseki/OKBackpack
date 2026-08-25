package ruiseki.okbackpack.common.item.infinity;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemSurvivalInfinityUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemSurvivalInfinityUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemSurvivalInfinityUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "survival_infinity_upgrade",
            null,
            config -> new ItemSurvivalInfinityUpgrade());
    }

}
