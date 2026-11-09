package ruiseki.backpack.init.item.crafting;

import ruiseki.okbackpack.GeneralConfig;
import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;
import ruiseki.okstoragecore.upgrades.crafting.ItemCraftingUpgrade;

public class ItemCraftingUpgradeConfig extends ItemConfig {

    public static ItemCraftingUpgradeConfig _instance;

    public ItemCraftingUpgradeConfig() {
        super(
            OKBackpack._instance,
            true,
            "crafting_upgrade",
            null,
            config -> new ItemCraftingUpgrade(GeneralConfig.maxUpgradesPerStorageConfig));
    }
}
