package ruiseki.okbackpack.common.item.travelers.magma;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemMagmaCubeUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemMagmaCubeUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemMagmaCubeUpgradeConfig() {
        super(OKBackpack._instance, true, "magma_cube_upgrade", null, config -> new ItemMagmaCubeUpgrade());
    }

}
