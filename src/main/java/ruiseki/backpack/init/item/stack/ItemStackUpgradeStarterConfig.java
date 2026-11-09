package ruiseki.backpack.init.item.stack;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.stack.ItemStackUpgrade;

public class ItemStackUpgradeStarterConfig extends ItemConfig {

    public static ItemStackUpgradeStarterConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.stack",
        comment = "Stack multiplier for Starter Stack Upgrade.",
        configLocation = ConfigLocation.SERVER)
    public static double stackUpgradeStarterMul = 1.5;

    public ItemStackUpgradeStarterConfig() {
        super(
            OKBackpack._instance,
            true,
            "stack_upgrade_starter",
            null,
            config -> new ItemStackUpgrade(() -> stackUpgradeStarterMul, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
