package ruiseki.okbackpack.common.item.travelers.sponge;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemSpongeUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemSpongeUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemSpongeUpgradeConfig() {
        super(OKBackpack._instance, true, "sponge_upgrade", null, config -> new ItemSpongeUpgrade());
    }

}
