package ruiseki.okbackpack.common.block;

import static ruiseki.okbackpack.common.init.TierRegistries.LEATHER;

import net.minecraft.item.Item;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.ConfigurableTypeCategory;
import ruiseki.okcore.config.extendedconfig.BlockContainerConfig;

public class BlockLeatherBackpackConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockLeatherBackpackConfig _instance;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.BLOCK,
        comment = "Number of item slots for Leather Backpack.")
    public static int leatherBackpackSlots = 27;

    @ConfigurableProperty(
        category = ConfigurableTypeCategory.BLOCK,
        comment = "Number of upgrade slots for Leather Backpack.")
    public static int leatherUpgradeSlots = 1;

    /**
     * Make a new instance.
     */
    public BlockLeatherBackpackConfig() {
        super(
            OKBackpack._instance,
            true,
            "leather_backpack",
            null,
            config -> new BlockBackpack(TierRegistry.getTier(LEATHER)));
    }

    @Override
    public Class<? extends Item> getItemBlockClass() {
        return BlockBackpack.ItemBackpack.class;
    }
}
