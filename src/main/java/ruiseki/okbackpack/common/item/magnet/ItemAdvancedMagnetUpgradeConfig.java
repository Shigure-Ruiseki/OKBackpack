package ruiseki.okbackpack.common.item.magnet;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvancedMagnetUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvancedMagnetUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvancedMagnetUpgradeConfig() {
        super(OKBackpack._instance, true, "advanced_magnet_upgrade", null, config -> new ItemAdvancedMagnetUpgrade());
    }

}
