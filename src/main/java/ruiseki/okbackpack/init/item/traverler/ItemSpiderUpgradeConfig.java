package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.spider.ItemSpiderUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemSpiderUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemSpiderUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemSpiderUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "spider_upgrade",
            null,
            config -> new ItemSpiderUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.spider_upgrade.tooltip"))));
    }

}
