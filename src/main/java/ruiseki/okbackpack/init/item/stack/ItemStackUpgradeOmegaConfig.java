package ruiseki.okbackpack.init.item.stack;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.stack.ItemStackUpgrade;

public class ItemStackUpgradeOmegaConfig extends ItemConfig {

    public static ItemStackUpgradeOmegaConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.stack",
        comment = "Stack multiplier for Omega Stack Upgrade.",
        configLocation = ConfigLocation.SERVER)
    public static double stackUpgradeOmegaMul = 33554431.0;

    public ItemStackUpgradeOmegaConfig() {
        super(
            OKBackpack._instance,
            true,
            "stack_upgrade_omega",
            null,
            config -> new ItemStackUpgrade(() -> stackUpgradeOmegaMul, GeneralConfig.maxUpgradesPerStorageConfig)
                .tooltipBuilder(
                    builder -> builder.add(
                        LangHelpers.localize("item.items.okbackpack.stack_upgrade.tooltip", stackUpgradeOmegaMul))));
    }
}
