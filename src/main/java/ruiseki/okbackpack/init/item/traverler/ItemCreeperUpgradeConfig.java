package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.creeper.ItemCreeperUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemCreeperUpgradeConfig extends ItemConfig {

    public static ItemCreeperUpgradeConfig _instance;

    public ItemCreeperUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "creeper_upgrade",
            null,
            config -> new ItemCreeperUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.creeper_upgrade.tooltip"))));
    }
}
