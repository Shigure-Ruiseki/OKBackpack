package ruiseki.backpack.init.item.stack;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.stack.ItemStackUpgrade;

public class ItemStackUpgradeTier1Config extends ItemConfig {

    public static ItemStackUpgradeTier1Config _instance;

    @ConfigurableProperty(
        category = "upgrade.stack",
        comment = "Stack multiplier for Tier 1 Stack Upgrade.",
        configLocation = ConfigLocation.SERVER)
    public static double stackUpgradeTier1Mul = 2.0;

    public ItemStackUpgradeTier1Config() {
        super(
            OKBackpack._instance,
            true,
            "stack_upgrade_tier_1",
            null,
            config -> new ItemStackUpgrade(() -> stackUpgradeTier1Mul, GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(
                    builder -> builder.addLine("item.items.okbackpack.stack_upgrade.tooltip", stackUpgradeTier1Mul)));
    }
}
