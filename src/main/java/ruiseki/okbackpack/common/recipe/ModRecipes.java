package ruiseki.okbackpack.common.recipe;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.GameRegistry;
import ruiseki.okbackpack.common.block.BlockDiamondBackpackConfig;
import ruiseki.okbackpack.common.block.BlockGoldBackpackConfig;
import ruiseki.okbackpack.common.block.BlockIronBackpackConfig;
import ruiseki.okbackpack.common.block.BlockLeatherBackpackConfig;
import ruiseki.okbackpack.common.block.BlockObsidianBackpackConfig;
import ruiseki.okbackpack.common.helpers.UpgradeFeatureHelper;
import ruiseki.okbackpack.common.item.ItemUpgradeConfig;
import ruiseki.okbackpack.common.item.arcane.ItemArcaneCraftingUpgradeConfig;
import ruiseki.okbackpack.common.item.energizednode.ItemEnergizedNodeUpgradeConfig;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okcore.enums.EnumDye;
import ruiseki.okcore.init.IInitListener;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.config.ConfigBlocks;

public class ModRecipes implements IInitListener {

    @Override
    public void onInit(Step step) {
        if (step == Step.POSTINIT) {
            blockRecipes();
            if (Mods.Thaumcraft.isModLoaded()) {
                thaumcraftRecipes();
            }
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
            new BackpackUpgradeRecipe(
                new ItemStack(BlockIronBackpackConfig._instance.getInstance()),
                BlockIronBackpackConfig.ironBackpackSlots,
                BlockIronBackpackConfig.ironUpgradeSlots,
                "CCC",
                "CBC",
                "CCC",
                'C',
                "ingotIron",
                'B',
                new ItemStack(BlockLeatherBackpackConfig._instance.getInstance())));

        // Gold Backpack
        GameRegistry.addRecipe(
            new BackpackUpgradeRecipe(
                new ItemStack(BlockGoldBackpackConfig._instance.getInstance()),
                BlockGoldBackpackConfig.goldBackpackSlots,
                BlockGoldBackpackConfig.goldUpgradeSlots,
                "CCC",
                "CBC",
                "CCC",
                'C',
                "ingotGold",
                'B',
                new ItemStack(BlockIronBackpackConfig._instance.getInstance())));

        // Diamond Backpack
        GameRegistry.addRecipe(
            new BackpackUpgradeRecipe(
                new ItemStack(BlockDiamondBackpackConfig._instance.getInstance()),
                BlockDiamondBackpackConfig.diamondBackpackSlots,
                BlockDiamondBackpackConfig.diamondUpgradeSlots,
                "CCC",
                "CBC",
                "CCC",
                'C',
                "gemDiamond",
                'B',
                new ItemStack(BlockGoldBackpackConfig._instance.getInstance())));

        // Obsidian Backpack
        GameRegistry.addRecipe(
            new BackpackUpgradeRecipe(
                new ItemStack(BlockObsidianBackpackConfig._instance.getInstance()),
                BlockObsidianBackpackConfig.obsidianBackpackSlots,
                BlockObsidianBackpackConfig.obsidianUpgradeSlots,
                "CSC",
                "SBS",
                "CSC",
                'S',
                "itemNetherStar",
                'C',
                "blockObsidian",
                'B',
                new ItemStack(BlockDiamondBackpackConfig._instance.getInstance())));

        // Dye Recipes
        BackpackDyeRecipes recipes = new BackpackDyeRecipes();

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {

                String accentOre = EnumDye.DYE_ORE_NAMES[i];
                String mainOre = EnumDye.DYE_ORE_NAMES[j];

                int accentColor = EnumDye.fromIndex(i)
                    .getColor();
                int mainColor = EnumDye.fromIndex(j)
                    .getColor();

                recipes.registerDyeRecipes(
                    new ItemStack(BlockLeatherBackpackConfig._instance.getInstance()),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);

                recipes.registerDyeRecipes(
                    new ItemStack(BlockIronBackpackConfig._instance.getInstance()),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);

                recipes.registerDyeRecipes(
                    new ItemStack(BlockGoldBackpackConfig._instance.getInstance()),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);

                recipes.registerDyeRecipes(
                    new ItemStack(BlockDiamondBackpackConfig._instance.getInstance()),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);

                recipes.registerDyeRecipes(
                    new ItemStack(BlockObsidianBackpackConfig._instance.getInstance()),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);
            }
        }
    }

    @Optional.Method(modid = "Thaumcraft")
    private static void thaumcraftRecipes() {
        if (UpgradeFeatureHelper.isArcaneCraftingUpgradeEnabled()) {
            // Arcane Crafting Upgrade
            ThaumcraftApi.addArcaneCraftingRecipe(
                "ARCTABLE",
                new ItemStack(ItemArcaneCraftingUpgradeConfig._instance.getInstance()),
                new AspectList().add(Aspect.AIR, 10)
                    .add(Aspect.EARTH, 10)
                    .add(Aspect.FIRE, 10)
                    .add(Aspect.WATER, 10)
                    .add(Aspect.ORDER, 10)
                    .add(Aspect.ENTROPY, 10),
                " A ",
                "IUI",
                " C ",
                'A',
                new ItemStack(ConfigBlocks.blockTable, 1, 15),
                'C',
                new ItemStack(Blocks.chest, 1, 0),
                'I',
                "ingotIron",
                'U',
                new ItemStack(ItemUpgradeConfig._instance.getInstance()));
        }

        // Energized Node Upgrade
        ThaumcraftApi.addArcaneCraftingRecipe(
            "VISPOWER",
            new ItemStack(ItemEnergizedNodeUpgradeConfig._instance.getInstance()),
            new AspectList().add(Aspect.AIR, 50)
                .add(Aspect.EARTH, 50)
                .add(Aspect.FIRE, 50)
                .add(Aspect.WATER, 50)
                .add(Aspect.ORDER, 50)
                .add(Aspect.ENTROPY, 50),
            " T ",
            "RUC",
            " S ",
            'T',
            new ItemStack(ConfigBlocks.blockStoneDevice, 1, 11),
            'R',
            new ItemStack(ConfigBlocks.blockMetalDevice, 1, 14),
            'C',
            new ItemStack(ConfigBlocks.blockMetalDevice, 1, 2),
            'S',
            new ItemStack(ConfigBlocks.blockStoneDevice, 1, 10),
            'U',
            new ItemStack(ItemUpgradeConfig._instance.getInstance()));
    }

}
