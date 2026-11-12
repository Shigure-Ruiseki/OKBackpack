package ruiseki.backpack.init.item.deposit;

import ruiseki.backpack.upgrades.deposit.ItemDepositUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemDepositUpgradeConfig extends ItemConfig {

    public static ItemDepositUpgradeConfig _instance;

    public ItemDepositUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "deposit_upgrade",
            null,
            config -> new ItemDepositUpgrade(() -> 9, GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.deposit_upgrade.tooltip")));
    }
}
