package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.lapis.ItemLapisUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemLapisUpgradeConfig extends ItemConfig {

    public static ItemLapisUpgradeConfig _instance;

    public ItemLapisUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "lapis_upgrade",
            null,
            config -> new ItemLapisUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.lapis_upgrade.tooltip"))));
    }

}
