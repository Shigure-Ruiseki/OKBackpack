package ruiseki.okbackpack.common.block;

import static ruiseki.okbackpack.common.init.TierRegistries.GOLD;

import net.minecraft.item.Item;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.ConfigurableTypeCategory;
import ruiseki.okcore.config.extendedconfig.BlockContainerConfig;

public class BlockGoldBackpackConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockGoldBackpackConfig _instance;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.BLOCK,
        comment = "Number of item slots for Gold Backpack.")
    public static int goldBackpackSlots = 81;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.BLOCK,
        comment = "Number of upgrade slots for Gold Backpack.")
    public static int goldUpgradeSlots = 3;

    /**
     * Make a new instance.
     */
    public BlockGoldBackpackConfig() {
        super(
            OKBackpack._instance,
            true,
            "gold_backpack",
            null,
            config -> new BlockBackpack(TierRegistry.getTier(GOLD)));
    }

    @Override
    public Class<? extends Item> getItemBlockClass() {
        return BlockBackpack.ItemBackpack.class;
    }
}
