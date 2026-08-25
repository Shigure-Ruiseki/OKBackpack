package ruiseki.okbackpack.common.item.arcane;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemArcaneCraftingUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemArcaneCraftingUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemArcaneCraftingUpgradeConfig() {
        super(OKBackpack._instance, true, "arcane_crafting_upgrade", null, config -> new ItemArcaneCraftingUpgrade());
    }

}
