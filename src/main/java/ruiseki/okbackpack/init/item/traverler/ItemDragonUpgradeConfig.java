package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.dragon.ItemDragonUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemDragonUpgradeConfig extends ItemConfig {

    public static ItemDragonUpgradeConfig _instance;

    public ItemDragonUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "dragon_upgrade",
            null,
            config -> new ItemDragonUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.dragon_upgrade.tooltip"))));
    }
}
