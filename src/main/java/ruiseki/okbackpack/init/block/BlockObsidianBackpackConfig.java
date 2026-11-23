package ruiseki.okbackpack.init.block;

import static ruiseki.okbackpack.init.TierRegistries.OBSIDIAN;

import net.minecraft.item.Item;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.backpack.BlockBackpack;
import ruiseki.okbackpack.backpack.ItemBackpack;
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
        super(OKBackpack._instance, true, "obsidian_backpack", null, config -> new BlockBackpack(OBSIDIAN));
    }

    @Override
    public Class<? extends Item> getItemBlockClass() {
        return ItemBackpack.class;
    }
}
