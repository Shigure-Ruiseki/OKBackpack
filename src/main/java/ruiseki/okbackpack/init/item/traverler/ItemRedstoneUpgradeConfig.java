package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.redstone.ItemRedstoneUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemRedstoneUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemRedstoneUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemRedstoneUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "redstone_upgrade",
            null,
            config -> new ItemRedstoneUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.redstone_upgrade.tooltip"))));
    }

}
