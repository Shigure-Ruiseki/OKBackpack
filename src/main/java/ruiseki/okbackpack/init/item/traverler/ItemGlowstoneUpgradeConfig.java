package ruiseki.okbackpack.init.item.traverler;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.upgrades.traverler.glowstone.ItemGlowstoneUpgrade;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemGlowstoneUpgradeConfig extends ItemConfig {

    public static ItemGlowstoneUpgradeConfig _instance;

    public ItemGlowstoneUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "glowstone_upgrade",
            null,
            config -> new ItemGlowstoneUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.glowstone_upgrade.tooltip"))));
    }

}
