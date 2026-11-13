package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.redstone.ItemRedstoneUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemRedstoneUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemRedstoneUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemRedstoneUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "redstone_upgrade",
            null,
            config -> new ItemRedstoneUpgrade(GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.redstone_upgrade.tooltip")));
    }

}
