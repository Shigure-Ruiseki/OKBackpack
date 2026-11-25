package ruiseki.okbackpack.common.item.travelers.bookshelf;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemBookshelfUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemBookshelfUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemBookshelfUpgradeConfig() {
        super(OKBackpack._instance, true, "bookshelf_upgrade", null, config -> new ItemBookshelfUpgrade());
    }

}
