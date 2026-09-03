package ruiseki.okbackpack.common.block;

import static ruiseki.okbackpack.common.init.TierRegistries.DIAMOND;

import net.minecraft.item.Item;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.BlockContainerConfig;

public class BlockDiamondBackpackConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockDiamondBackpackConfig _instance;

    @ConfigurableProperty(category = "block", comment = "Number of item slots for Diamond Backpack.")
    public static int diamondBackpackSlots = 108;
    @ConfigurableProperty(category = "block", comment = "Number of upgrade slots for Diamond Backpack.")
    public static int diamondUpgradeSlots = 5;

    /**
     * Make a new instance.
     */
    public BlockDiamondBackpackConfig() {
        super(
            OKBackpack._instance,
            true,
            "diamond_backpack",
            null,
            config -> new BlockBackpack(TierRegistry.getTier(DIAMOND)));
    }

    @Override
    public Class<? extends Item> getItemBlockClass() {
        return BlockBackpack.ItemBackpack.class;
    }
}
