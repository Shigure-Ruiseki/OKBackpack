package ruiseki.okbackpack.common.item.jukebox;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemAdvancedJukeBoxUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemAdvancedJukeBoxUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemAdvancedJukeBoxUpgradeConfig() {
        super(OKBackpack._instance, true, "advanced_jukebox_upgrade", null, config -> new ItemAdvancedJukeboxUpgrade());
    }

}
