package ruiseki.backpack.init.item.inception;

import ruiseki.backpack.upgrades.inception.ItemInceptionUpgrade;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemInceptionUpgradeConfig extends ItemConfig {

    public static ItemInceptionUpgradeConfig _instance;

    public ItemInceptionUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "inception_upgrade",
            null,
            config -> new ItemInceptionUpgrade().tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.inception_upgrade.tooltip"))));
    }
}
