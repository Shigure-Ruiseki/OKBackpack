package ruiseki.okbackpack.common.item.travelers.glowstone;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemGlowstoneUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemGlowstoneUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemGlowstoneUpgradeConfig() {
        super(OKBackpack._instance, true, "glowstone_upgrade", null, config -> new ItemGlowstoneUpgrade());
    }

}
