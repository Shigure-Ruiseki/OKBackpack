package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.creeper.ItemCreeperUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemCreeperUpgradeConfig extends ItemConfig {

    public static ItemCreeperUpgradeConfig _instance;

    public ItemCreeperUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "creeper_upgrade",
            null,
            config -> new ItemCreeperUpgrade(GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
