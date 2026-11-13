package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.ghast.ItemGhastUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemGhastUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemGhastUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemGhastUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "ghast_upgrade",
            null,
            config -> new ItemGhastUpgrade(GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.ghast_upgrade.tooltip")));
    }

}
