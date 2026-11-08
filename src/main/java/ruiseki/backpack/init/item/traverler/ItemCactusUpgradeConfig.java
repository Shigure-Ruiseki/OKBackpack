package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.cactus.ItemCactusUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemCactusUpgradeConfig extends ItemConfig {

    public static ItemCactusUpgradeConfig _instance;

    public ItemCactusUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "cactus_upgrade",
            null,
            config -> new ItemCactusUpgrade(GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
