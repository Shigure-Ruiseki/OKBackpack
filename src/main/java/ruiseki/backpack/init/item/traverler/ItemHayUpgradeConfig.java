package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.hay.ItemHayUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemHayUpgradeConfig extends ItemConfig {

    public static ItemHayUpgradeConfig _instance;

    public ItemHayUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "hay_upgrade",
            null,
            config -> new ItemHayUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.addLine("item.items.okbackpack.hay_upgrade.tooltip")
                    .addLine("item.items.okbackpack.hay_upgrade.tooltip.1")));
    }

}
