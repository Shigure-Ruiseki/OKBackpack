package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.bat.ItemBatUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemBlazeUpgradeConfig extends ItemConfig {

    public static ItemBlazeUpgradeConfig _instance;

    public ItemBlazeUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "blaze_upgrade",
            null,
            config -> new ItemBatUpgrade(GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
