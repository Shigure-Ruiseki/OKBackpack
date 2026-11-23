package ruiseki.okbackpack.init.item.infinity;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.infinity.ItemInfinityUpgrade;

public class ItemInfinityUpgradeConfig extends ItemConfig {

    public static ItemInfinityUpgradeConfig _instance;

    public ItemInfinityUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "infinity_upgrade",
            null,
            config -> new ItemInfinityUpgrade(GeneralConfig.maxUpgradesPerStorageConfig, true).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.infinity_upgrade.tooltip"))
                    .add(LangHelpers.localize("item.items.okbackpack.infinity_upgrade.tooltip.1"))));
    }
}
