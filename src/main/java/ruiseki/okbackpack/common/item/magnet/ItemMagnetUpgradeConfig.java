package ruiseki.okbackpack.common.item.magnet;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemMagnetUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemMagnetUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemMagnetUpgradeConfig() {
        super(OKBackpack._instance, true, "magnet_upgrade", null, config -> new ItemMagnetUpgrade());
    }

}
