package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.slime.ItemSlimeUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemSlimeUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemSlimeUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemSlimeUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "slime_upgrade",
            null,
            config -> new ItemSlimeUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.addLine("item.items.okbackpack.slime_upgrade.tooltip")
                    .addLine("item.items.okbackpack.slime_upgrade.tooltip.1")));
    }

}
