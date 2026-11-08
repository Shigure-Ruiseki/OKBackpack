package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.bat.ItemBatUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemBatUpgradeConfig extends ItemConfig {

    public static ItemBatUpgradeConfig _instance;

    public ItemBatUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "bat_upgrade",
            null,
            config -> new ItemBatUpgrade(GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
