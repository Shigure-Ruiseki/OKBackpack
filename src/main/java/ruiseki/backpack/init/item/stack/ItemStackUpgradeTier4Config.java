package ruiseki.backpack.init.item.stack;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.stack.ItemStackUpgrade;

public class ItemStackUpgradeTier4Config extends ItemConfig {

    public static ItemStackUpgradeTier4Config _instance;

    @ConfigurableProperty(
        category = "upgrade.stack",
        comment = "Stack multiplier for Tier 4 Stack Upgrade.",
        configLocation = ConfigLocation.SERVER)
    public static double stackUpgradeTier4Mul = 16.0;

    public ItemStackUpgradeTier4Config() {
        super(
            OKBackpack._instance,
            true,
            "stack_upgrade_tier_4",
            null,
            config -> new ItemStackUpgrade(() -> stackUpgradeTier4Mul, GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(
                    builder -> builder.addLine("item.items.okbackpack.stack_upgrade.tooltip", stackUpgradeTier4Mul)));
    }
}
