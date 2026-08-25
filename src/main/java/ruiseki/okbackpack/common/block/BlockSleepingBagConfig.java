package ruiseki.okbackpack.common.block;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.config.extendedconfig.BlockConfig;

public class BlockSleepingBagConfig extends BlockConfig {

    /**
     * The unique instance.
     */
    public static BlockSleepingBagConfig _instance;

    /**
     * Make a new instance.
     */
    public BlockSleepingBagConfig() {
        super(OKBackpack._instance, true, "sleeping_bag", null, config -> new BlockSleepingBag());
    }
}
