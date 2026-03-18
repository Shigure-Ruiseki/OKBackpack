package ruiseki.okbackpack.common.init;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import ruiseki.okcore.init.IInitListener;

public class ModOreDicts implements IInitListener {

    @Override
    public void onInit(Step step) {
        if (step == Step.POSTINIT) {
            register("dustRedstone", new ItemStack(Items.redstone));
            register("itemNetherStar", new ItemStack(Items.nether_star));
            register("itemGhastTear", new ItemStack(Items.ghast_tear));
            register("pearlEnder", new ItemStack(Items.ender_pearl));
            register("itemClay", new ItemStack(Items.clay_ball));
            register("itemLeather", new ItemStack(Items.leather));
            register("blockObsidian", Blocks.obsidian);
            register("blockHopper", Blocks.hopper);
            register("barsIron", Blocks.iron_bars);
            register("chestWood", Blocks.chest);
        }
    }

    private static void register(String key, ItemStack stack) {
        OreDictionary.registerOre(key, stack);
    }

    private static void register(String key, Block block) {
        OreDictionary.registerOre(key, block);
    }
}
