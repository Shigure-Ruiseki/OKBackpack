package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.cake.ItemCakeUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemCakeUpgradeConfig extends ItemConfig {

    public static ItemCakeUpgradeConfig _instance;

    public ItemCakeUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "cake_upgrade",
            null,
            config -> new ItemCakeUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.cake_upgrade.tooltip"))));
    }
}
