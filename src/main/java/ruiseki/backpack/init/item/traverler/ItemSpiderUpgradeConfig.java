package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.spider.ItemSpiderUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

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
            config -> new ItemSpiderUpgrade(GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.spider_upgrade.tooltip")));
    }

}
