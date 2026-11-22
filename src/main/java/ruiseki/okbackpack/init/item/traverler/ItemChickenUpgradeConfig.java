package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.chicken.ItemChickenUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemChickenUpgradeConfig extends ItemConfig {

    public static ItemChickenUpgradeConfig _instance;

    public ItemChickenUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "chicken_upgrade",
            null,
            config -> new ItemChickenUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.chicken_upgrade.tooltip"))));
    }
}
