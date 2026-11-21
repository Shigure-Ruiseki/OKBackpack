package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.wolf.ItemWolfUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemWolfUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemWolfUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemWolfUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "wolf_upgrade",
            null,
            config -> new ItemWolfUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.wolf_upgrade.tooltip"))
                    .add(LangHelpers.localize("item.items.okbackpack.wolf_upgrade.tooltip.1"))));
    }

}
