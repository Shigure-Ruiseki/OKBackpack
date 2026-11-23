package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.hay.ItemHayUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemHayUpgradeConfig extends ItemConfig {

    public static ItemHayUpgradeConfig _instance;

    public ItemHayUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "hay_upgrade",
            null,
            config -> new ItemHayUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.hay_upgrade.tooltip"))
                    .add(LangHelpers.localize("item.items.okbackpack.hay_upgrade.tooltip.1"))));
    }

}
