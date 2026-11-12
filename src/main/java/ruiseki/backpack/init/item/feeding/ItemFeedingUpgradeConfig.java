package ruiseki.backpack.init.item.feeding;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.feeding.ItemFeedingUpgrade;

public class ItemFeedingUpgradeConfig extends ItemConfig {

    public static ItemFeedingUpgradeConfig _instance;

    public ItemFeedingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "feeding_upgrade",
            null,
            config -> new ItemFeedingUpgrade(() -> 9, GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.feeding_upgrade.tooltip")));
    }
}
