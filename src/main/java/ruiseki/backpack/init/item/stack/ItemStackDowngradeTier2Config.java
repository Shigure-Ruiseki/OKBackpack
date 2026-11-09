package ruiseki.backpack.init.item.stack;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.stack.ItemStackUpgrade;

public class ItemStackDowngradeTier2Config extends ItemConfig {

    public static ItemStackDowngradeTier2Config _instance;

    @ConfigurableProperty(
        category = "upgrade.stack",
        comment = "Stack multiplier for Tier 2 Stack Downgrade.",
        configLocation = ConfigLocation.SERVER)
    public static double stackDowngradeTier2Mul = 0.25;

    public ItemStackDowngradeTier2Config() {
        super(
            OKBackpack._instance,
            true,
            "stack_downgrade_tier_2",
            null,
            config -> new ItemStackUpgrade(() -> stackDowngradeTier2Mul, GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
