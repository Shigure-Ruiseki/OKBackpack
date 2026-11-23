package ruiseki.okbackpack.init.item.deposit;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.deposit.ItemDepositUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemDepositUpgradeConfig extends ItemConfig {

    public static ItemDepositUpgradeConfig _instance;

    public ItemDepositUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "deposit_upgrade",
            null,
            config -> new ItemDepositUpgrade(() -> 9, GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.deposit_upgrade.tooltip"))));
    }
}
