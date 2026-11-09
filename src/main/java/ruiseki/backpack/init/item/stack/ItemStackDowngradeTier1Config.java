package ruiseki.backpack.init.item.stack;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.stack.ItemStackUpgrade;

public class ItemStackDowngradeTier1Config extends ItemConfig {

    public static ItemStackDowngradeTier1Config _instance;

    @ConfigurableProperty(
        category = "upgrade.stack",
        comment = "Stack multiplier for Tier 1 Stack Downgrade.",
        configLocation = ConfigLocation.SERVER)
    public static double stackDowngradeTier1Mul = 0.5;

    public ItemStackDowngradeTier1Config() {
        super(
            OKBackpack._instance,
            true,
            "stack_downgrade_tier_1",
            null,
            config -> new ItemStackUpgrade(() -> stackDowngradeTier1Mul, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
