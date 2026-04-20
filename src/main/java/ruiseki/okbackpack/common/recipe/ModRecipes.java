package ruiseki.okbackpack.common.recipe;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.GameRegistry;
import ruiseki.okbackpack.common.helpers.UpgradeFeatureHelper;
import ruiseki.okbackpack.common.init.ModBlocks;
import ruiseki.okbackpack.common.init.ModItems;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.config.ModConfig;
import ruiseki.okcore.color.EnumDye;
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
            itemRecipes();
            if (ModConfig.enableTravelersUpgrades) {
                travelersRecipes();
            }
            if (Mods.Thaumcraft.isModLoaded()) {
                thaumcraftRecipes();
            }
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
        if (!Mods.EtFuturum.isModLoaded()) {
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

        // Stack Upgrade Starter Tier
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.STACK_UPGRADE.newItemStack(1, 5),
                "BBB",
                "BUB",
                "BBB",
                'B',
                "blockCopper",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Stack Downgrade Tier 1
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.STACK_UPGRADE.newItemStack(1, 6),
                "SFS",
                "SUS",
                "FSF",
                'F',
                Items.flint,
                'S',
                Items.stick,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Stack Downgrade Tier 2
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.STACK_UPGRADE.newItemStack(1, 7),
                "FSF",
                "SUS",
                "FSF",
                'F',
                Items.flint,
                'S',
                Items.stick,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Stack Downgrade Tier 3
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.STACK_UPGRADE.newItemStack(1, 8),
                "SFS",
                "FUF",
                "FSF",
                'F',
                Items.flint,
                'S',
                Items.stick,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

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

        // Jukebox Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.JUKEBOX_UPGRADE.getItem(),
                " J ",
                "IUI",
                " R ",
                'J',
                Blocks.jukebox,
                'R',
                "dustRedstone",
                'I',
                "ingotIron",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Advanced Jukebox Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.ADVANCED_JUKEBOX_UPGRADE.getItem(),
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
                ModItems.JUKEBOX_UPGRADE.getItem()));

        // Tool Swapper Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.TOOL_SWAPPER_UPGRADE.getItem(),
                "RSR",
                "PUA",
                "IHI",
                'S',
                Items.wooden_sword,
                'P',
                Items.wooden_pickaxe,
                'A',
                Items.wooden_axe,
                'H',
                Items.wooden_shovel,
                'R',
                "dustRedstone",
                'I',
                "ingotIron",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Advanced Tool Swapper Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.ADVANCED_TOOL_SWAPPER_UPGRADE.getItem(),
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
                ModItems.TOOL_SWAPPER_UPGRADE.getItem()));

        // Anvil Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.ANVIL_UPGRADE.getItem(),
                "ADA",
                "IUI",
                " C ",
                'A',
                Blocks.anvil,
                'C',
                Blocks.chest,
                'D',
                "gemDiamond",
                'I',
                "ingotIron",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Refill Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.REFILL_UPGRADE.getItem(),
                " E ",
                "IUI",
                "RCR",
                'E',
                Items.ender_pearl,
                'C',
                Blocks.chest,
                'R',
                "dustRedstone",
                'I',
                "ingotIron",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Advanced Refill Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.ADVANCED_REFILL_UPGRADE.getItem(),
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
                ModItems.REFILL_UPGRADE.getItem()));

        // Smelting Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.SMELTING_UPGRADE.getItem(),
                "RIR",
                "IUI",
                "RFR",
                'F',
                Blocks.furnace,
                'R',
                "dustRedstone",
                'I',
                "ingotIron",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Auto Smelting Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.AUTO_SMELTING_UPGRADE.getItem(),
                "DHD",
                "RUH",
                "GHG",
                'H',
                Blocks.hopper,
                'D',
                "gemDiamond",
                'R',
                "dustRedstone",
                'G',
                "ingotGold",
                'U',
                ModItems.SMELTING_UPGRADE.getItem()));

        // Everlasting Upgrade
        if (!Mods.EtFuturum.isModLoaded()) {
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

            // Blasting Upgrade
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    ModItems.BLASTING_UPGRADE.getItem(),
                    "III",
                    "IUI",
                    "SSS",
                    'S',
                    ganymedes01.etfuturum.ModBlocks.SMOOTH_STONE.get(),
                    'I',
                    "ingotIron",
                    'U',
                    ModItems.SMELTING_UPGRADE.getItem()));

            // Auto Blasting Upgrade
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    ModItems.AUTO_BLASTING_UPGRADE.getItem(),
                    "DHD",
                    "RUH",
                    "GHG",
                    'H',
                    Blocks.hopper,
                    'D',
                    "gemDiamond",
                    'R',
                    "dustRedstone",
                    'G',
                    "ingotGold",
                    'U',
                    ModItems.BLASTING_UPGRADE.getItem()));

            // Smoking Upgrade
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    ModItems.SMOKING_UPGRADE.getItem(),
                    " L ",
                    "LUL",
                    " L ",
                    'L',
                    "logWood",
                    'U',
                    ModItems.SMELTING_UPGRADE.getItem()));

            // Auto Smoking Upgrade
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    ModItems.AUTO_SMOKING_UPGRADE.getItem(),
                    "DHD",
                    "RUH",
                    "GHG",
                    'H',
                    Blocks.hopper,
                    'D',
                    "gemDiamond",
                    'R',
                    "dustRedstone",
                    'G',
                    "ingotGold",
                    'U',
                    ModItems.SMOKING_UPGRADE.getItem()));

            // Tank Upgrade
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    ModItems.TANK_UPGRADE.getItem(),
                    "GGG",
                    "GUG",
                    "GGG",
                    'G',
                    "blockGlass",
                    'U',
                    ModItems.BASE_UPGRADE.getItem()));

            // Battery Upgrade
            GameRegistry.addRecipe(
                new ShapedOreRecipe(
                    ModItems.BATTERY_UPGRADE.getItem(),
                    "GRG",
                    "RUR",
                    "GRG",
                    'G',
                    "ingotGold",
                    'R',
                    "blockRedstone",
                    'U',
                    ModItems.BASE_UPGRADE.getItem()));

        }
    }

    private static void travelersRecipes() {
        // Redstone Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.REDSTONE_UPGRADE.getItem(),
                "DBD",
                "BUB",
                "DBD",
                'D',
                "dustRedstone",
                'B',
                "blockRedstone",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Glowstone Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.GLOWSTONE_UPGRADE.getItem(),
                "DBD",
                "BUB",
                "DBD",
                'D',
                "dustGlowstone",
                'B',
                "glowstone",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Rainbow Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.RAINBOW_UPGRADE.getItem(),
                "RTP",
                "OUB",
                "YGC",
                'R',
                "dyeRed",
                'T',
                Items.record_cat,
                'P',
                "dyePurple",
                'O',
                "dyeOrange",
                'B',
                "dyeBlue",
                'Y',
                "dyeYellow",
                'G',
                "dyeGreen",
                'C',
                "dyeCyan",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Cactus Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.CACTUS_UPGRADE.getItem(),
                "CGC",
                "CUC",
                "SSS",
                'C',
                Blocks.cactus,
                'G',
                "dyeGreen",
                'S',
                "sand",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Cow Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.COW_UPGRADE.getItem(),
                "BLB",
                "BUB",
                "LML",
                'B',
                Items.beef,
                'L',
                Items.leather,
                'M',
                Items.milk_bucket,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Bat Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.BAT_UPGRADE.getItem(),
                "SNS",
                "SUS",
                "SRS",
                'S',
                "stone",
                'R',
                Items.golden_carrot,
                'N',
                new ItemStack(Items.potionitem, 1, 8262),
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Squid Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.SQUID_UPGRADE.getItem(),
                "INI",
                "BUB",
                "IWI",
                'B',
                Items.water_bucket,
                'I',
                Items.dye,
                'W',
                new ItemStack(Items.potionitem, 1, 8269),
                'N',
                new ItemStack(Items.potionitem, 1, 8262),
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Wither Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.WITHER_UPGRADE.getItem(),
                "BWB",
                "SUS",
                "BSB",
                'B',
                "coal",
                'W',
                new ItemStack(Items.skull, 1, 1),
                'S',
                "soulSand",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Cake Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.CAKE_UPGRADE.getItem(),
                "ECE",
                "WUW",
                "SMS",
                'E',
                Items.egg,
                'C',
                Items.cake,
                'W',
                Items.wheat,
                'S',
                Items.sugar,
                'M',
                Items.milk_bucket,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Slime Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.SLIME_UPGRADE.getItem(),
                "SDS",
                "SUS",
                "SPS",
                'S',
                "slimeball",
                'D',
                new ItemStack(Items.potionitem, 1, 8194),
                'P',
                Blocks.sticky_piston,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Bookshelf Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.BOOKSHELF_UPGRADE.getItem(),
                "S S",
                "SUS",
                "BBB",
                'S',
                Blocks.bookshelf,
                'B',
                Items.book,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Wolf Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.WOLF_UPGRADE.getItem(),
                "BWB",
                "WUW",
                "BWB",
                'B',
                Items.bone,
                'W',
                Blocks.wool,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Ocelot Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.OCELOT_UPGRADE.getItem(),
                "WFW",
                "FUF",
                "WFW",
                'F',
                Items.fish,
                'W',
                new ItemStack(Blocks.wool, 1, 4),
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Quiver Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.QUIVER_UPGRADE.getItem(),
                "AL ",
                "LUL",
                " LS",
                'A',
                Items.arrow,
                'L',
                Items.leather,
                'S',
                Items.string,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Chicken Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.CHICKEN_UPGRADE.getItem(),
                "EEE",
                "EUE",
                "EEE",
                'E',
                Items.egg,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Magma Cube Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.MAGMA_CUBE_UPGRADE.getItem(),
                "MLM",
                "MUM",
                "MLM",
                'M',
                Items.magma_cream,
                'L',
                Items.lava_bucket,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Dragon Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.DRAGON_UPGRADE.getItem(),
                "EDE",
                "OUO",
                "POP",
                'E',
                Blocks.end_stone,
                'D',
                Blocks.dragon_egg,
                'O',
                Blocks.obsidian,
                'P',
                Items.ender_pearl,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Blaze Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.BLAZE_UPGRADE.getItem(),
                "RFR",
                "RUR",
                "PLP",
                'R',
                Items.blaze_rod,
                'F',
                Items.fire_charge,
                'P',
                Items.blaze_powder,
                'L',
                Items.lava_bucket,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Sponge Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.SPONGE_UPGRADE.getItem(),
                "SSS",
                "SUS",
                "SSS",
                'S',
                Blocks.sponge,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Creeper Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.CREEPER_UPGRADE.getItem(),
                "GCG",
                "GUG",
                "TTT",
                'G',
                Items.gunpowder,
                'C',
                new ItemStack(Items.skull, 1, 4),
                'T',
                Blocks.tnt,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Ghast Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.GHAST_UPGRADE.getItem(),
                "TFT",
                "GUG",
                "TGT",
                'T',
                Items.ghast_tear,
                'G',
                Items.gunpowder,
                'F',
                Items.fire_charge,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Spider Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.SPIDER_UPGRADE.getItem(),
                "ESE",
                "SUS",
                "ESE",
                'E',
                Items.spider_eye,
                'S',
                Items.string,
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Lapis Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.LAPIS_UPGRADE.getItem(),
                "BGB",
                "GUG",
                "BGB",
                'B',
                "blockLapis",
                'G',
                "gemLapis",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Quartz Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.QUARTZ_UPGRADE.getItem(),
                "BGB",
                "GUG",
                "BGB",
                'B',
                "blockQuartz",
                'G',
                "gemQuartz",
                'U',
                ModItems.BASE_UPGRADE.getItem()));

        // Hay Upgrade
        GameRegistry.addRecipe(
            new ShapedOreRecipe(
                ModItems.HAY_UPGRADE.getItem(),
                "WWW",
                "WUW",
                "WWW",
                'W',
                Items.wheat,
                'U',
                ModItems.BASE_UPGRADE.getItem()));
    }

    @Optional.Method(modid = "Thaumcraft")
    private static void thaumcraftRecipes() {
        if (UpgradeFeatureHelper.isArcaneCraftingUpgradeEnabled()) {
            // Arcane Crafting Upgrade
            ThaumcraftApi.addArcaneCraftingRecipe(
                "ARCTABLE",
                ModItems.ARCANE_CRAFTING_UPGRADE.newItemStack(),
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
                ModItems.BASE_UPGRADE.getItem());
        }

        // Energized Node Upgrade
        ThaumcraftApi.addArcaneCraftingRecipe(
            "VISPOWER",
            ModItems.ENERGIZED_NODE_UPGRADE.newItemStack(),
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
            ModItems.BASE_UPGRADE.getItem());
    }

}
