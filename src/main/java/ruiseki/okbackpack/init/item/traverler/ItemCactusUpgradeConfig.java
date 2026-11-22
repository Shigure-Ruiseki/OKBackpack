package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.cactus.ItemCactusUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemCactusUpgradeConfig extends ItemConfig {

    public static ItemCactusUpgradeConfig _instance;

    public ItemCactusUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "cactus_upgrade",
            null,
            config -> new ItemCactusUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.cactus_upgrade.tooltip"))));
    }
}
