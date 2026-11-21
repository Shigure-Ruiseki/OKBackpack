package ruiseki.backpack.init.item.restock;

import ruiseki.backpack.upgrades.restock.ItemRestockUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemAdvRestockUpgradeConfig extends ItemConfig {

    public static ItemAdvRestockUpgradeConfig _instance;

    public ItemAdvRestockUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_restock_upgrade",
            null,
            config -> new ItemRestockUpgrade(() -> 16, GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.advanced_restock_upgrade.tooltip"))
                    .add(LangHelpers.localize("item.items.okbackpack.advanced_restock_upgrade.tooltip.1"))));
    }
}
