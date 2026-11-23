package ruiseki.okbackpack.init.item.voiding;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.ConfigLocation;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.voiding.ItemVoidUpgrade;

public class ItemAdvVoidUpgradeConfig extends ItemConfig {

    public static ItemAdvVoidUpgradeConfig _instance;

    @ConfigurableProperty(
        category = "upgrade.void",
        comment = "Enable voiding any item",
        configLocation = ConfigLocation.SERVER)
    public static boolean advVoidAnythingEnabled = true;

    public ItemAdvVoidUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "advanced_void_upgrade",
            null,
            config -> new ItemVoidUpgrade(
                () -> advVoidAnythingEnabled,
                () -> 16,
                GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                    builder -> builder.add(LangHelpers.localize("item.items.okbackpack.advanced_void_upgrade.tooltip"))
                        .add(LangHelpers.localize("item.items.okbackpack.advanced_void_upgrade.tooltip.1"))));
    }
}
