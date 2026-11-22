package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.ghast.ItemGhastUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemGhastUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemGhastUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemGhastUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "ghast_upgrade",
            null,
            config -> new ItemGhastUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.ghast_upgrade.tooltip"))));
    }

}
