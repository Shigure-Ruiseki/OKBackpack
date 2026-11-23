package ruiseki.okbackpack.init.item.magnet;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.magnet.ItemMagnetUpgrade;

public class ItemAdvMagnetUpgradeConfig extends ItemConfig {

    public static ItemAdvMagnetUpgradeConfig _instance;

    @ConfigurableProperty(category = "upgrade.magnet", comment = "Magnet Range", configLocation = ConfigLocation.SERVER)
    public static int AdvMagnetRange = 5;

    public ItemAdvMagnetUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_magnet_upgrade",
            null,
            config -> new ItemMagnetUpgrade(() -> AdvMagnetRange, () -> 16, GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(
                    builder -> builder
                        .add(LangHelpers.localize("item.items.okbackpack.advanced_magnet_upgrade.tooltip"))
                        .add(LangHelpers.localize("item.items.okbackpack.advanced_magnet_upgrade.tooltip.1"))));
    }
}
