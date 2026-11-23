package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.cow.ItemCowUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemCowUpgradeConfig extends ItemConfig {

    public static ItemCowUpgradeConfig _instance;

    public ItemCowUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "cow_upgrade",
            null,
            config -> new ItemCowUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.cow_upgrade.tooltip"))));
    }
}
