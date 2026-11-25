package ruiseki.okbackpack.common.item.energizednode;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemEnergizedNodeUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemEnergizedNodeUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemEnergizedNodeUpgradeConfig() {
        super(OKBackpack._instance, true, "energized_node_upgrade", null, config -> new ItemEnergizedNodeUpgrade());
    }

}
