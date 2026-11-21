package ruiseki.backpack.init.item.traverler;

import ruiseki.backpack.upgrades.traverler.magma.ItemMagmaCubeUpgrade;
import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;

public class ItemMagmaCubeUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemMagmaCubeUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemMagmaCubeUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "magma_cube_upgrade",
            null,
            config -> new ItemMagmaCubeUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.magma_cube_upgrade.tooltip"))));
    }

}
