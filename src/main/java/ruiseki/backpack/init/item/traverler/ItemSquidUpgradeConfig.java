package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.squid.ItemSquidUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemSquidUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemSquidUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemSquidUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "squid_upgrade",
            null,
            config -> new ItemSquidUpgrade(GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.squid_upgrade.tooltip")));
    }

}
