package ruiseki.backpack.init.item.voiding;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.voiding.ItemVoidUpgrade;

public class ItemAdvVoidUpgradeTest extends ItemConfig {

    public static ItemAdvVoidUpgradeTest _instance;

    @ConfigurableProperty(
        category = "upgrade.void",
        comment = "Enable voiding any item",
        configLocation = ConfigLocation.SERVER)
    public static boolean voidAnythingEnabled = true;

    public ItemAdvVoidUpgradeTest() {
        super(
            OKBackpack._instance,
            true,
            "adv_void_upgrade_test",
            null,
            config -> new ItemVoidUpgrade(
                () -> voidAnythingEnabled,
                () -> 16,
                GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
