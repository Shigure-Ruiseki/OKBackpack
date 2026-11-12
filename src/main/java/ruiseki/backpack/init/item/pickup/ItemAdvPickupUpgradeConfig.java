package ruiseki.backpack.init.item.pickup;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.pickup.ItemPickupUpgrade;

public class ItemAdvPickupUpgradeConfig extends ItemConfig {

    public static ItemAdvPickupUpgradeConfig _instance;

    public ItemAdvPickupUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_pickup_upgrade",
            null,
            config -> new ItemPickupUpgrade(() -> 16, GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.addLine("item.items.okbackpack.advanced_pickup_upgrade.tooltip")
                    .addLine("item.items.okbackpack.advanced_pickup_upgrade.tooltip.1")));
    }
}
