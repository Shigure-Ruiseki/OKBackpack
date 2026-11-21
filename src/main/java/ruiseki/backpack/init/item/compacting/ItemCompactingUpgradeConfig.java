package ruiseki.backpack.init.item.compacting;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.compacting.ItemCompactingUpgrade;

public class ItemCompactingUpgradeConfig extends ItemConfig {

    public static ItemCompactingUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.compacting",
        comment = "Compact 3x3.",
        configLocation = ConfigLocation.SERVER)
    public static boolean basicShouldCompactThreeByThree = true;

    public ItemCompactingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "compacting_upgrade",
            null,
            config -> new ItemCompactingUpgrade(
                () -> basicShouldCompactThreeByThree,
                () -> 9,
                GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                    builder -> builder.add(LangHelpers.localize("item.items.okbackpack.compacting_upgrade.tooltip"))
                        .add(LangHelpers.localize("item.items.okbackpack.compacting_upgrade.tooltip.1"))));
    }
}
