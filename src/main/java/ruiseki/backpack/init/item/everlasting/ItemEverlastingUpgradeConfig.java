package ruiseki.backpack.init.item.everlasting;

import ruiseki.backpack.upgrades.everlasting.ItemEverlastingUpgrade;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemEverlastingUpgradeConfig extends ItemConfig {

    public static ItemEverlastingUpgradeConfig _instance;

    public ItemEverlastingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "everlasting_upgrade",
            null,
            config -> new ItemEverlastingUpgrade().tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.everlasting_upgrade.tooltip"))
                    .add(LangHelpers.localize("item.items.okbackpack.everlasting_upgrade.tooltip.1"))));
    }
}
