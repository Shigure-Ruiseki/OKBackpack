package ruiseki.okbackpack.common.item.travelers.rainbow;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemRainbowUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemRainbowUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemRainbowUpgradeConfig() {
        super(OKBackpack._instance, true, "rainbow_upgrade", null, config -> new ItemRainbowUpgrade());
    }

}
