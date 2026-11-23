package ruiseki.okbackpack.init.item.restock;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.restock.ItemRestockUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemRestockUpgradeConfig extends ItemConfig {

    public static ItemRestockUpgradeConfig _instance;

    public ItemRestockUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "restock_upgrade",
            null,
            config -> new ItemRestockUpgrade(() -> 9, GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.restock_upgrade.tooltip"))));
    }
}
