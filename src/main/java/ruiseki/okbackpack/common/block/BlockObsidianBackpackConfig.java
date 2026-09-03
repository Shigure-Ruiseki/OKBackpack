package ruiseki.okbackpack.common.block;

import static ruiseki.okbackpack.common.init.TierRegistries.LEATHER;

import net.minecraft.item.Item;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okcore.config.ConfigurableProperty;
import ruiseki.okcore.config.extendedconfig.BlockContainerConfig;

public class BlockObsidianBackpackConfig extends BlockContainerConfig {

    /**
     * The unique instance.
     */
    public static BlockObsidianBackpackConfig _instance;

    @ConfigurableProperty(category = "block", comment = "Number of item slots for Obsidian Backpack.")
    public static int obsidianBackpackSlots = 120;

    @ConfigurableProperty(category = "block", comment = "Number of upgrade slots for Obsidian Backpack.")
    public static int obsidianUpgradeSlots = 7;

    /**
     * Make a new instance.
     */
    public BlockObsidianBackpackConfig() {
        super(
            OKBackpack._instance,
            true,
            "obsidian_backpack",
            null,
            config -> new BlockBackpack(TierRegistry.getTier(LEATHER)));
    }

    @Override
    public Class<? extends Item> getItemBlockClass() {
        return BlockBackpack.ItemBackpack.class;
    }
}
