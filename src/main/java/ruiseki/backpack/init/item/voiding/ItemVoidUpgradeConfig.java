package ruiseki.backpack.init.item.voiding;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.voiding.ItemVoidUpgrade;

public class ItemVoidUpgradeConfig extends ItemConfig {

    public static ItemVoidUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.void",
        comment = "Enable voiding any item",
        configLocation = ConfigLocation.SERVER)
    public static boolean basicVoidAnythingEnabled = true;

    public ItemVoidUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "void_upgrade",
            null,
            config -> new ItemVoidUpgrade(
                () -> basicVoidAnythingEnabled,
                () -> 9,
                GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
