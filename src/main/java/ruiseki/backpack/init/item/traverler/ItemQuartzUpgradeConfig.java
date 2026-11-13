package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.quartz.ItemQuartzUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemQuartzUpgradeConfig extends ItemConfig {

    public static ItemQuartzUpgradeConfig _instance;

    public ItemQuartzUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "quartz_upgrade",
            null,
            config -> new ItemQuartzUpgrade(GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.quartz_upgrade.tooltip")));
    }

}
