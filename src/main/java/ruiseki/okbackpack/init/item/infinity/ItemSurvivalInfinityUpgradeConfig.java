package ruiseki.okbackpack.init.item.infinity;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
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
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.survival_infinity_upgrade.tooltip"))
                    .add(LangHelpers.localize("item.items.okbackpack.survival_infinity_upgrade.tooltip.1"))));
    }
}
