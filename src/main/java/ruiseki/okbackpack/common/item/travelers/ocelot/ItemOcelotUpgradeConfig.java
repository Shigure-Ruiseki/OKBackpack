package ruiseki.okbackpack.common.item.travelers.ocelot;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemOcelotUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemOcelotUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemOcelotUpgradeConfig() {
        super(OKBackpack._instance, true, "ocelot_upgrade", null, config -> new ItemOcelotUpgrade());
    }

}
