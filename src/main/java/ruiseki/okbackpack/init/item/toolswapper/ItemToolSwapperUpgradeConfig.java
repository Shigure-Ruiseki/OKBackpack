package ruiseki.okbackpack.init.item.toolswapper;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.toolswapper.ItemToolSwapperUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemToolSwapperUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemToolSwapperUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemToolSwapperUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "tool_swapper_upgrade",
            null,
            config -> new ItemToolSwapperUpgrade(() -> 9, () -> true, () -> true).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.tool_swapper_upgrade.tooltip"))));
    }

}
