package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.cow.ItemCowUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemCowUpgradeConfig extends ItemConfig {

    public static ItemCowUpgradeConfig _instance;

    public ItemCowUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "cow_upgrade",
            null,
            config -> new ItemCowUpgrade(GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.cow_upgrade.tooltip")));
    }
}
