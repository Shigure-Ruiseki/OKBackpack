package ruiseki.okbackpack.common.recipe;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.registry.GameRegistry;
import ruiseki.okbackpack.common.init.ModBlocks;
import ruiseki.okbackpack.common.init.ModItems;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.config.ModConfig;
import ruiseki.okcore.enums.EnumDye;
import ruiseki.okcore.init.IInitListener;

public class ModRecipes implements IInitListener {

    @Override
    public void onInit(Step step) {
        if (step == Step.POSTINIT) {
            blockRecipes();
            itemRecipes();
        }
    }

    public static void blockRecipes() {

        // Leather Backpack
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModBlocks.BACKPACK_BASE.newItemStack(),
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
                ModBlocks.BACKPACK_IRON.newItemStack(),
                ModConfig.ironBackpackSlots,
                ModConfig.ironUpgradeSlots,
                "CCC",
                "CBC",
                "CCC",
                'C',
                "ingotIron",
                'B',
                ModBlocks.BACKPACK_BASE.getItem()));

        // Gold Backpack
        GameRegistry.addRecipe(
            new BackpackUpgradeRecipe(
                ModBlocks.BACKPACK_GOLD.newItemStack(),
                ModConfig.goldBackpackSlots,
                ModConfig.goldUpgradeSlots,
                "CCC",
                "CBC",
                "CCC",
                'C',
                "ingotGold",
                'B',
                ModBlocks.BACKPACK_IRON.getItem()));

        // Diamond Backpack
        GameRegistry.addRecipe(
            new BackpackUpgradeRecipe(
                ModBlocks.BACKPACK_DIAMOND.newItemStack(),
                ModConfig.diamondBackpackSlots,
                ModConfig.diamondUpgradeSlots,
                "CCC",
                "CBC",
                "CCC",
                'C',
                "gemDiamond",
                'B',
                ModBlocks.BACKPACK_GOLD.getItem()));

        // Obsidian Backpack
        GameRegistry.addRecipe(
            new BackpackUpgradeRecipe(
                ModBlocks.BACKPACK_OBSIDIAN.newItemStack(),
                ModConfig.obsidianBackpackSlots,
                ModConfig.obsidianUpgradeSlots,
                "CSC",
                "SBS",
                "CSC",
                'S',
                "itemNetherStar",
                'C',
                "blockObsidian",
                'B',
                ModBlocks.BACKPACK_DIAMOND.getItem()));

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
                    ModBlocks.BACKPACK_BASE.newItemStack(),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);

                recipes.registerDyeRecipes(
                    ModBlocks.BACKPACK_IRON.newItemStack(),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);

                recipes.registerDyeRecipes(
                    ModBlocks.BACKPACK_GOLD.newItemStack(),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);

                recipes.registerDyeRecipes(
                    ModBlocks.BACKPACK_DIAMOND.newItemStack(),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);

                recipes.registerDyeRecipes(
                    ModBlocks.BACKPACK_OBSIDIAN.newItemStack(),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);
            }
        }
    }

    public static void itemRecipes() {

        // Upgrade Base
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.BASE_UPGRADE.getItem(),
                "SIS",
                "ILI",
                "SIS",
                'S',
                new ItemStack(Items.string, 1, 0),
                'I',
                "ingotIron",
                'L',
                new ItemStack(Items.leather, 1, 0)));

        // Stack Upgrade Tier 1
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.STACK_UPGRADE.newItemStack(1, 0),
                "BBB",
                "BUB",
                "BBB",
                'B',
                "blockIron",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Stack Upgrade Tier 2
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.STACK_UPGRADE.newItemStack(1, 1),
                "BBB",
                "BUB",
                "BBB",
                'B',
                "blockGold",
                'U',
                ModItems.STACK_UPGRADE.newItemStack(1, 0)));

        // Stack Upgrade Tier 3
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.STACK_UPGRADE.newItemStack(1, 2),
                "BBB",
                "BUB",
                "BBB",
                'B',
                "blockDiamond",
                'U',
                ModItems.STACK_UPGRADE.newItemStack(1, 1)));

        // Stack Upgrade Tier 4
        if (!Mods.EtFuturum.isLoaded()) {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    ModItems.STACK_UPGRADE.newItemStack(1, 3),
                    "BBB",
                    "BUB",
                    "BBB",
                    'B',
                    "itemNetherStar",
                    'U',
                    ModItems.STACK_UPGRADE.newItemStack(1, 2)));
        } else {

            // Stack Upgrade Tier 4
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    ModItems.STACK_UPGRADE.newItemStack(1, 3),
                    "BBB",
                    "BUB",
                    "BBB",
                    'B',
                    "blockNetherite",
                    'U',
                    ModItems.STACK_UPGRADE.newItemStack(1, 2)));
        }

        // Stack Upgrade Tier Omega
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.STACK_UPGRADE.newItemStack(1, 4),
                "BBB",
                "BBB",
                "BBB",
                'B',
                ModItems.STACK_UPGRADE.newItemStack(1, 3)));

        // Crafting Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.CRAFTING_UPGRADE.getItem(),
                " c ",
                "IUI",
                " C ",
                'c',
                new ItemStack(Blocks.crafting_table, 1, 0),
                'C',
                new ItemStack(Blocks.chest, 1, 0),
                'I',
                "ingotIron",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Magnet Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.MAGNET_UPGRADE.getItem(),
                "EIE",
                "IUI",
                "R L",
                'E',
                "pearlEnder",
                'R',
                "dustRedstone",
                'L',
                "gemLapis",
                'I',
                "ingotIron",
                'U',
                ModItems.PICKUP_UPGRADE.getItem()));

        // Advanced Magnet Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.ADVANCED_MAGNET_UPGRADE.getItem(),
                "EIE",
                "IUI",
                "R L",
                'E',
                "pearlEnder",
                'R',
                "dustRedstone",
                'L',
                "gemLapis",
                'I',
                "ingotIron",
                'U',
                ModItems.ADVANCED_PICKUP_UPGRADE.getItem()));

        // Advanced Magnet Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.ADVANCED_MAGNET_UPGRADE.getItem(),
                " D ",
                "GUG",
                "RRR",
                'D',
                "gemDiamond",
                'R',
                "dustRedstone",
                'G',
                "ingotGold",
                'U',
                ModItems.ADVANCED_PICKUP_UPGRADE.getItem()));

        // Void Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.VOID_UPGRADE.newItemStack(),
                " E ",
                "OUO",
                "ROR",
                'E',
                "pearlEnder",
                'R',
                "dustRedstone",
                'O',
                "blockObsidian",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Advanced Void Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.ADVANCED_VOID_UPGRADE.getItem(),
                " D ",
                "GUG",
                "RRR",
                'D',
                "gemDiamond",
                'R',
                "dustRedstone",
                'G',
                "ingotGold",
                'U',
                ModItems.VOID_UPGRADE.getItem()));

        // Feeding Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.FEEDING_UPGRADE.getItem(),
                " C ",
                "AUM",
                " E ",
                'E',
                "pearlEnder",
                'C',
                new ItemStack(Items.golden_carrot, 1, 0),
                'A',
                new ItemStack(Items.golden_apple, 1, 0),
                'M',
                new ItemStack(Items.speckled_melon, 1, 0),
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Advanced Feeding Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.ADVANCED_FEEDING_UPGRADE.getItem(),
                " D ",
                "GUG",
                "RRR",
                'D',
                "gemDiamond",
                'R',
                "dustRedstone",
                'G',
                "ingotGold",
                'U',
                ModItems.FEEDING_UPGRADE.getItem()));

        // Pickup Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.PICKUP_UPGRADE.getItem(),
                " D ",
                "GUG",
                "RRR",
                'D',
                Blocks.sticky_piston,
                'R',
                "dustRedstone",
                'G',
                Items.string,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Advanced Pickup Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.ADVANCED_PICKUP_UPGRADE.getItem(),
                " D ",
                "GUG",
                "RRR",
                'D',
                "gemDiamond",
                'R',
                "dustRedstone",
                'G',
                "ingotGold",
                'U',
                ModItems.PICKUP_UPGRADE.getItem()));

        // Filter Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.FILTER_UPGRADE.getItem(),
                "RSR",
                "SUS",
                "RSR",
                'R',
                "dustRedstone",
                'S',
                Items.string,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Advanced Filter Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.ADVANCED_FILTER_UPGRADE.getItem(),
                " D ",
                "GUG",
                "RRR",
                'D',
                "gemDiamond",
                'R',
                "dustRedstone",
                'G',
                "ingotGold",
                'U',
                ModItems.FILTER_UPGRADE.getItem()));

        // Inception Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.INCEPTION_UPGRADE.getItem(),
                "ESE",
                "DUD",
                "EDE",
                'D',
                "gemDiamond",
                'S',
                "itemNetherStar",
                'E',
                Items.ender_eye,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Compacting Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.COMPACTING_UPGRADE.getItem(),
                "IPI",
                "PUP",
                "RPR",
                'P',
                Blocks.piston,
                'R',
                "dustRedstone",
                'I',
                "ingotIron",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Advanced Compacting Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.ADVANCED_COMPACTING_UPGRADE.getItem(),
                " D ",
                "GUG",
                "RRR",
                'D',
                "gemDiamond",
                'R',
                "dustRedstone",
                'G',
                "ingotGold",
                'U',
                ModItems.COMPACTING_UPGRADE.getItem()));

        // Everlasting Upgrade
        if (!Mods.EtFuturum.isLoaded()) {
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    ModItems.EVERLASTING_UPGRADE.getItem(),
                    "GRG",
                    "RUR",
                    "GRG",
                    'G',
                    "itemGhastTear",
                    'R',
                    "itemNetherStar",
                    'U',

                    ModItems.BASE_UPGRADE.getItem()));
        } else {

            // Everlasting Upgrade
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    ModItems.EVERLASTING_UPGRADE.getItem(),
                    "GRG",
                    "RUR",
                    "GRG",
                    'G',
                    ganymedes01.etfuturum.ModItems.END_CRYSTAL.get(),
                    'R',
                    "itemNetherStar",
                    'U',

                    ModItems.BASE_UPGRADE.getItem()));

        }
    }

}
