package ruiseki.backpack.init.item.infinity;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.infinity.ItemInfinityUpgrade;

public class ItemSurvivalInfinityUpgradeConfig extends ItemConfig {

    public static ItemSurvivalInfinityUpgradeConfig _instance;

    public ItemSurvivalInfinityUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "survival_infinity_upgrade",
            null,
            config -> new ItemInfinityUpgrade(GeneralConfig.maxUpgradesPerStorageConfig, false).tooltipBuilder(
                builder -> builder.addLine("item.items.okbackpack.survival_infinity_upgrade.tooltip")
                    .addLine("item.items.okbackpack.survival_infinity_upgrade.tooltip.1")));
    }
}
