package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.quartz.ItemQuartzUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemQuartzUpgradeConfig extends ItemConfig {

    public static ItemQuartzUpgradeConfig _instance;

    public ItemQuartzUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "quartz_upgrade",
            null,
            config -> new ItemQuartzUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.quartz_upgrade.tooltip"))));
    }

}
