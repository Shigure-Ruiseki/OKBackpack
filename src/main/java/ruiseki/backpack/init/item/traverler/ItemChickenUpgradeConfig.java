package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.chicken.ItemChickenUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemChickenUpgradeConfig extends ItemConfig {

    public static ItemChickenUpgradeConfig _instance;

    public ItemChickenUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "chicken_upgrade",
            null,
            config -> new ItemChickenUpgrade(GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.chicken_upgrade.tooltip")));
    }
}
