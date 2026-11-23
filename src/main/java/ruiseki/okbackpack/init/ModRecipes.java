package ruiseki.okbackpack.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.registry.GameRegistry;
import ruiseki.okbackpack.init.block.BlockDiamondBackpackConfig;
import ruiseki.okbackpack.init.block.BlockGoldBackpackConfig;
import ruiseki.okbackpack.init.block.BlockIronBackpackConfig;
import ruiseki.okbackpack.init.block.BlockLeatherBackpackConfig;
import ruiseki.okbackpack.init.block.BlockObsidianBackpackConfig;
import ruiseki.okcore.init.IInitListener;

public class ModRecipes implements IInitListener {

    @Override
    public void onInit(Step step) {
        if (step == Step.POSTINIT) {
            blockRecipes();
        }
    }

    public static void blockRecipes() {

        // Leather Backpack
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(BlockLeatherBackpackConfig._instance.getInstance()),
                "SLS",
                "SCS",
                "LLL",
                'S',
                Items.string,
                'L',
                "itemLeather",
                'C',
                Blocks.chest));

        // Iron Backpack
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(BlockIronBackpackConfig._instance.getInstance()),
                "CCC",
                "CBC",
                "CCC",
                'C',
                "ingotIron",
                'B',
                new ItemStack(BlockLeatherBackpackConfig._instance.getInstance())));

        // Gold Backpack
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(BlockGoldBackpackConfig._instance.getInstance()),
                "CCC",
                "CBC",
                "CCC",
                'C',
                "ingotGold",
                'B',
                new ItemStack(BlockIronBackpackConfig._instance.getInstance())));

        // Diamond Backpack
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(BlockDiamondBackpackConfig._instance.getInstance()),
                "CCC",
                "CBC",
                "CCC",
                'C',
                "gemDiamond",
                'B',
                new ItemStack(BlockGoldBackpackConfig._instance.getInstance())));

        // Obsidian Backpack
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                new ItemStack(BlockObsidianBackpackConfig._instance.getInstance()),
                "CSC",
                "SBS",
                "CSC",
                'S',
                "itemNetherStar",
                'C',
                "blockObsidian",
                'B',
                new ItemStack(BlockDiamondBackpackConfig._instance.getInstance())));
    }
}
