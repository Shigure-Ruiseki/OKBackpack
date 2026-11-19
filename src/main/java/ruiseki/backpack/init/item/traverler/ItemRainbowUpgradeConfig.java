package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.rainbow.ItemRainbowUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemRainbowUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemRainbowUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemRainbowUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "rainbow_upgrade",
            null,
            config -> new ItemRainbowUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.rainbow_upgrade.tooltip"))
                    .add(LangHelpers.localize("item.items.okbackpack.rainbow_upgrade.tooltip.1"))));
    }

}
