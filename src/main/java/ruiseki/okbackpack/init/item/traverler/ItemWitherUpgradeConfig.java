package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.wither.ItemWitherUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemWitherUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemWitherUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemWitherUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "wither_upgrade",
            null,
            config -> new ItemWitherUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.wither_upgrade.tooltip"))
                    .add(LangHelpers.localize("item.items.okbackpack.wither_upgrade.tooltip.1"))));
    }

}
