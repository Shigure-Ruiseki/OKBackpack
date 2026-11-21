package ruiseki.backpack.init.item.toolswapper;

import ruiseki.backpack.upgrades.toolswapper.ItemToolSwapperUpgrade;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemAdvToolSwapperUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvToolSwapperUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvToolSwapperUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_tool_swapper_upgrade",
            null,
            config -> new ItemToolSwapperUpgrade(() -> 16, () -> true, () -> true).tooltipBuilder(
                builder -> builder
                    .add(LangHelpers.localize("item.items.okbackpack.advanced_tool_swapper_upgrade.tooltip"))
                    .add(LangHelpers.localize("item.items.okbackpack.advanced_tool_swapper_upgrade.tooltip.1"))));
    }

}
