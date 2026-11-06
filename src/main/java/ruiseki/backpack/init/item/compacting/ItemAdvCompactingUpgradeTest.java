package ruiseki.backpack.init.item.compacting;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.compacting.ItemCompactingUpgrade;

public class ItemAdvCompactingUpgradeTest extends ItemConfig {

    public static ItemAdvCompactingUpgradeTest _instance;

    @ConfigurableProperty(
        category = "upgrade.compacting",
        comment = "Compact 3x3.",
        configLocation = ConfigLocation.SERVER)
    public static boolean shouldCompactThreeByThree = true;

    public ItemAdvCompactingUpgradeTest() {
        super(
            OKBackpack._instance,
            true,
            "adv_compacting_upgrade_test",
            null,
            config -> new ItemCompactingUpgrade(
                () -> shouldCompactThreeByThree,
                () -> 16,
                GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
