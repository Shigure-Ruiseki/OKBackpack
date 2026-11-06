package ruiseki.backpack.init.item.deposit;

import ruiseki.backpack.upgrades.deposit.ItemDepositUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvDepositUpgradeTest extends ItemConfig {

    public static ItemAdvDepositUpgradeTest _instance;

    public ItemAdvDepositUpgradeTest() {
        super(
            OKBackpack._instance,
            true,
            "adv_deposit_upgrade_test",
            null,
            config -> new ItemDepositUpgrade(() -> 16, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
