package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.quiver.ItemQuiverUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemQuiverUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemQuiverUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemQuiverUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "quiver_upgrade",
            null,
            config -> new ItemQuiverUpgrade(GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.quiver_upgrade.tooltip")));
    }

}
