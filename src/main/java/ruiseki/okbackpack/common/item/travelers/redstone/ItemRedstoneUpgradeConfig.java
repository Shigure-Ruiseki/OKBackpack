package ruiseki.okbackpack.common.item.travelers.redstone;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemRedstoneUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemRedstoneUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemRedstoneUpgradeConfig() {
        super(OKBackpack._instance, true, "redstone_upgrade", null, config -> new ItemRedstoneUpgrade());
    }

}
