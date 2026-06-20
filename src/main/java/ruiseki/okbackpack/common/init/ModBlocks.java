package ruiseki.okbackpack.common.init;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.Level;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.block.BlockSleepingBag;
import ruiseki.okcore.block.IBlock;

public enum ModBlocks {

    // spotless: off

    BACKPACK_BASE(new BlockBackpack(TierRegistry.getTier("leather_backpack"))),
    BACKPACK_IRON(new BlockBackpack(TierRegistry.getTier("iron_backpack"))),
    BACKPACK_GOLD(new BlockBackpack(TierRegistry.getTier("gold_backpack"))),
    BACKPACK_DIAMOND(new BlockBackpack(TierRegistry.getTier("diamond_backpack"))),
    BACKPACK_OBSIDIAN(new BlockBackpack(TierRegistry.getTier("obsidian_backpack"))),
    SLEEPING_BAG(new BlockSleepingBag()),

    ;

    // spotless: on

    public static final ModBlocks[] VALUES = values();

    public static void preInit() {
        for (ModBlocks block : VALUES) {
            if (block.block == null) {
                continue;
            }
            try {
                block.block.init();
                OKBackpack.okLog(Level.INFO, "Successfully initialized " + block.name());
            } catch (Exception e) {
                OKBackpack.okLog(Level.ERROR, "Failed to initialize block: +" + block.name());
            }
        }
    }

    private final IBlock block;

    ModBlocks(IBlock block) {
        this.block = block;
    }

    public Block getBlock() {
        return block.getBlock();
    }

    public Item getItem() {
        return block != null ? Item.getItemFromBlock(getBlock()) : null;
    }

    public ItemStack newItemStack() {
        return newItemStack(1);
    }

    public ItemStack newItemStack(int count) {
        return newItemStack(count, 0);
    }

    public ItemStack newItemStack(int count, int meta) {
        return block != null ? new ItemStack(this.getBlock(), count, meta) : null;
    }
}
