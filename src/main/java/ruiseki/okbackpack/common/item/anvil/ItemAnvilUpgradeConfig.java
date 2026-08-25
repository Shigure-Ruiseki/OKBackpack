package ruiseki.okbackpack.common.item.anvil;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAnvilUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAnvilUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAnvilUpgradeConfig() {
        super(OKBackpack._instance, true, "anvil_upgrade", null, config -> new ItemAnvilUpgrade());
    }

}
