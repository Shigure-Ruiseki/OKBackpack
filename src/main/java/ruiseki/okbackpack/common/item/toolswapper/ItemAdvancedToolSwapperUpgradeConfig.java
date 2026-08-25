package ruiseki.okbackpack.common.item.toolswapper;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvancedToolSwapperUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvancedToolSwapperUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvancedToolSwapperUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_tool_swapper_upgrade",
            null,
            config -> new ItemAdvancedToolSwapperUpgrade());
    }

}
