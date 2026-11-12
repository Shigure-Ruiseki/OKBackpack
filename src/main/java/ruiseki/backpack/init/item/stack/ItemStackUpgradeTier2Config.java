package ruiseki.backpack.init.item.stack;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.stack.ItemStackUpgrade;

public class ItemStackUpgradeTier2Config extends ItemConfig {

    public static ItemStackUpgradeTier2Config _instance;

    @ConfigurableProperty(
        category = "upgrade.stack",
        comment = "Stack multiplier for Tier 2 Stack Upgrade.",
        configLocation = ConfigLocation.SERVER)
    public static double stackUpgradeTier2Mul = 4.0;

    public ItemStackUpgradeTier2Config() {
        super(
            OKBackpack._instance,
            true,
            "stack_upgrade_tier_2",
            null,
            config -> new ItemStackUpgrade(() -> stackUpgradeTier2Mul, GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(
                    builder -> builder.addLine("item.items.okbackpack.stack_upgrade.tooltip", stackUpgradeTier2Mul)));
    }
}
