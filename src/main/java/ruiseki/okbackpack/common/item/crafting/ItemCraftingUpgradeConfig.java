package ruiseki.okbackpack.common.item.crafting;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemCraftingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemCraftingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemCraftingUpgradeConfig() {
        super(OKBackpack._instance, true, "crafting_upgrade", null, config -> new ItemCraftingUpgrade());
    }

}
