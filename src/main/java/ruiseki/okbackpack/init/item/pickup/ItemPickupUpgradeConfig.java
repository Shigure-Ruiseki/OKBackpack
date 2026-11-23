package ruiseki.okbackpack.init.item.pickup;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.pickup.ItemPickupUpgrade;

public class ItemPickupUpgradeConfig extends ItemConfig {

    public static ItemPickupUpgradeConfig _instance;

    public ItemPickupUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "pickup_upgrade",
            null,
            config -> new ItemPickupUpgrade(() -> 9, GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.pickup_upgrade.tooltip"))));
    }
}
