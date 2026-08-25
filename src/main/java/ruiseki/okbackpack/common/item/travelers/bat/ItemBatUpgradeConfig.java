package ruiseki.okbackpack.common.item.travelers.bat;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemBatUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemBatUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemBatUpgradeConfig() {
        super(OKBackpack._instance, true, "bat_upgrade", null, config -> new ItemBatUpgrade());
    }

}
