package ruiseki.okbackpack.common.block;

import static ruiseki.okbackpack.common.init.TierRegistries.IRON;

import net.minecraft.item.Item;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.BlockContainerConfig;

public class BlockIronBackpackConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockIronBackpackConfig _instance;

    @ConfigurableProperty(category = "block", comment = "Number of item slots for Iron Backpack.")
    public static int ironBackpackSlots = 54;
    @ConfigurableProperty(category = "block", comment = "Number of upgrade slots for Iron Backpack.")
    public static int ironUpgradeSlots = 2;

    /**
     * Make a new instance.
     */
    public BlockIronBackpackConfig() {
        super(
            OKBackpack._instance,
            true,
            "iron_backpack",
            null,
            config -> new BlockBackpack(TierRegistry.getTier(IRON)));
    }

    @Override
    public Class<? extends Item> getItemBlockClass() {
        return BlockBackpack.ItemBackpack.class;
    }
}
