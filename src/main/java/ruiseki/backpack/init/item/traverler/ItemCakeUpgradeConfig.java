package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.cake.ItemCakeUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemCakeUpgradeConfig extends ItemConfig {

    public static ItemCakeUpgradeConfig _instance;

    public ItemCakeUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "cake_upgrade",
            null,
            config -> new ItemCakeUpgrade(GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.cake_upgrade.tooltip")));
    }
}
