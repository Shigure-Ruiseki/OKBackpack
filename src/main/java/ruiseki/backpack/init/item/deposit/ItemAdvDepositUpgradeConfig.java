package ruiseki.backpack.init.item.deposit;

import ruiseki.backpack.upgrades.deposit.ItemDepositUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemAdvDepositUpgradeConfig extends ItemConfig {

    public static ItemAdvDepositUpgradeConfig _instance;

    public ItemAdvDepositUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_deposit_upgrade",
            null,
            config -> new ItemDepositUpgrade(() -> 16, GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.advanced_deposit_upgrade.tooltip"))
                    .add(LangHelpers.localize("item.items.okbackpack.advanced_deposit_upgrade.tooltip.1"))));
    }
}
