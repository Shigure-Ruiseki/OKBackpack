package ruiseki.okbackpack.common.item.toolswapper;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemToolSwapperUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemToolSwapperUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemToolSwapperUpgradeConfig() {
        super(OKBackpack._instance, true, "tool_swapper_upgrade", null, config -> new ItemToolSwapperUpgrade());
    }

}
