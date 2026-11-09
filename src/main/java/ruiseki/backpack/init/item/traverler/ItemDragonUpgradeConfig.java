package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.dragon.ItemDragonUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemDragonUpgradeConfig extends ItemConfig {

    public static ItemDragonUpgradeConfig _instance;

    public ItemDragonUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "dragon_upgrade",
            null,
            config -> new ItemDragonUpgrade(GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
