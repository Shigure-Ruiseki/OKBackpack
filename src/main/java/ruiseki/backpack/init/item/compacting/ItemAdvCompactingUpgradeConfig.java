package ruiseki.backpack.init.item.compacting;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.compacting.ItemCompactingUpgrade;

public class ItemAdvCompactingUpgradeConfig extends ItemConfig {

    public static ItemAdvCompactingUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.compacting",
        comment = "Compact 3x3.",
        configLocation = ConfigLocation.SERVER)
    public static boolean AdvShouldCompactThreeByThree = true;

    public ItemAdvCompactingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_compacting_upgrade",
            null,
            config -> new ItemCompactingUpgrade(
                () -> AdvShouldCompactThreeByThree,
                () -> 16,
                GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
