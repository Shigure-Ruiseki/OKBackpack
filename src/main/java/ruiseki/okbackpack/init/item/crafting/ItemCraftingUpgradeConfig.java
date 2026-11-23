package ruiseki.okbackpack.init.item.crafting;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okstoragecore.upgrades.crafting.ItemCraftingUpgrade;

public class ItemCraftingUpgradeConfig extends ItemConfig {

    public static ItemCraftingUpgradeConfig _instance;

    public ItemCraftingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "crafting_upgrade",
            null,
            config -> new ItemCraftingUpgrade(GeneralConfig.maxUpgradesPerStorageConfig).tooltipBuilder(
                builder -> builder.add(LangHelpers.localize("item.items.okbackpack.crafting_upgrade.tooltip"))));
    }
}
