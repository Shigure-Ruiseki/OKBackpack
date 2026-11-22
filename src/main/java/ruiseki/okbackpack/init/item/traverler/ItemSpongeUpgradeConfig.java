package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.sponge.ItemSpongeUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemSpongeUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemSpongeUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemSpongeUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "sponge_upgrade",
            null,
            config -> new ItemSpongeUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.sponge_upgrade.tooltip"))));
    }

}
