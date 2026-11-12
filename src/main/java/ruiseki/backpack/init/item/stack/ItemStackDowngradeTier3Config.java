package ruiseki.backpack.init.item.stack;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.stack.ItemStackUpgrade;

public class ItemStackDowngradeTier3Config extends ItemConfig {

    public static ItemStackDowngradeTier3Config _instance;

    @ConfigurableProperty(
        category = "upgrade.stack",
        comment = "Stack multiplier for Tier 3 Stack Downgrade.",
        configLocation = ConfigLocation.SERVER)
    public static double stackDowngradeTier3Mul = 0.125;

    public ItemStackDowngradeTier3Config() {
        super(
            OKBackpack._instance,
            true,
            "stack_downgrade_tier_3",
            null,
            config -> new ItemStackUpgrade(() -> stackDowngradeTier3Mul, GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(
                    builder -> builder
                        .addLine("item.items.okbackpack.stack_downgrade.tooltip", stackDowngradeTier3Mul)));
    }
}
