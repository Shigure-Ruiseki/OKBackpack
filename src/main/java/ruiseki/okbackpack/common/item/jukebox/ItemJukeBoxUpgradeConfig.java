package ruiseki.okbackpack.common.item.jukebox;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.ItemConfig;

public class ItemJukeBoxUpgradeConfig extends ItemConfig {

    /**
     * The unique instance.
     */
    public static ItemJukeBoxUpgradeConfig _instance;

    /**
     * Make a new instance.
     */
    public ItemJukeBoxUpgradeConfig() {
        super(OKBackpack._instance, true, "jukebox_upgrade", null, config -> new ItemJukeboxUpgrade());
    }

}
