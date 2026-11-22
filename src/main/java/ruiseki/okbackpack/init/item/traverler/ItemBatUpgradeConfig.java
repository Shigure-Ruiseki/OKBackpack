package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.bat.ItemBatUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemBatUpgradeConfig extends ItemConfig {

    public static ItemBatUpgradeConfig _instance;

    public ItemBatUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "bat_upgrade",
            null,
            config -> new ItemBatUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.bat_upgrade.tooltip"))));
    }
}
