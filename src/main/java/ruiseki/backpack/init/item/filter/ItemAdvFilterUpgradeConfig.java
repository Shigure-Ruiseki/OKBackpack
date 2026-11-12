package ruiseki.backpack.init.item.filter;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.filter.ItemFilterUpgrade;

public class ItemAdvFilterUpgradeConfig extends ItemConfig {

    public static ItemAdvFilterUpgradeConfig _instance;

    public ItemAdvFilterUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_filter_upgrade",
            null,
            config -> new ItemFilterUpgrade(() -> 16, GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.addLine("item.items.okbackpack.advanced_filter_upgrade.tooltip")
                    .addLine("item.items.okbackpack.advanced_filter_upgrade.tooltip.1")));
    }
}
