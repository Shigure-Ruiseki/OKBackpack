package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.ocelot.ItemOcelotUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemOcelotUpgradeConfig extends ItemConfig {

    public static ItemOcelotUpgradeConfig _instance;

    public ItemOcelotUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "ocelot_upgrade",
            null,
            config -> new ItemOcelotUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.ocelot_upgrade.tooltip"))));
    }

}
