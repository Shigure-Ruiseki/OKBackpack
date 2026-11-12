package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.glowstone.ItemGlowstoneUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemGlowstoneUpgradeConfig extends ItemConfig {

    public static ItemGlowstoneUpgradeConfig _instance;

    public ItemGlowstoneUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "glowstone_upgrade",
            null,
            config -> new ItemGlowstoneUpgrade(GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(builder -> builder.addLine("item.items.okbackpack.glowstone_upgrade.tooltip")));
    }

}
