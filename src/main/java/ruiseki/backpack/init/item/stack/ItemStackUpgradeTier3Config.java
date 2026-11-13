package ruiseki.backpack.init.item.stack;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.stack.ItemStackUpgrade;

public class ItemStackUpgradeTier3Config extends ItemConfig {

    public static ItemStackUpgradeTier3Config _instance;

    @ConfigurableProperty(
        category = "upgrade.stack",
        comment = "Stack multiplier for Tier 3 Stack Upgrade.",
        configLocation = ConfigLocation.SERVER)
    public static double stackUpgradeTier3Mul = 8.0;

    public ItemStackUpgradeTier3Config() {
        super(
            OKBackpack._instance,
            true,
            "stack_upgrade_tier_3",
            null,
            config -> new ItemStackUpgrade(() -> stackUpgradeTier3Mul, GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(
                    builder -> builder.addLine("item.items.okbackpack.stack_upgrade.tooltip", stackUpgradeTier3Mul)));
    }
}
