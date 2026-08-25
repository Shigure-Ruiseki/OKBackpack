package ruiseki.okbackpack.common.item.stack;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.ConfigurableTypeCategory;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemStackUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemStackUpgradeConfig _instance;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.ITEM,
        comment = "Stack multiplier for Starter Stack Upgrade.")
    public static double stackUpgradeStarterMul = 1.5;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.ITEM,
        comment = "Stack multiplier for Tier 1 Stack Upgrade.")
    public static double stackUpgradeTier1Mul = 2.0;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.ITEM,
        comment = "Stack multiplier for Tier 2 Stack Upgrade.")
    public static double stackUpgradeTier2Mul = 4.0;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.ITEM,
        comment = "Stack multiplier for Tier 3 Stack Upgrade.")
    public static double stackUpgradeTier3Mul = 8.0;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.ITEM,
        comment = "Stack multiplier for Tier 4 Stack Upgrade.")
    public static double stackUpgradeTier4Mul = 16.0;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.ITEM,
        comment = "Stack multiplier for Omega Stack Upgrade.")
    public static double stackUpgradeTierOmegaMul = 33554431.0;

    @ConfigurableProperty(category = ConfigurableTypeCategory.ITEM, comment = "Stack divisor for Tier 1 Downgrade.")
    public static int stackDowngradeTier1Div = 8;

    @ConfigurableProperty(category = ConfigurableTypeCategory.ITEM, comment = "Stack divisor for Tier 2 Downgrade.")
    public static int stackDowngradeTier2Div = 16;

    @ConfigurableProperty(category = ConfigurableTypeCategory.ITEM, comment = "Stack divisor for Tier 3 Downgrade.")
    public static int stackDowngradeTier3Div = 32;

    /**
     * Make a new instance.
     */
    public ItemStackUpgradeConfig() {
        super(OKBackpack._instance, true, "stack_upgrade", null, config -> new ItemStackUpgrade());
    }

}
