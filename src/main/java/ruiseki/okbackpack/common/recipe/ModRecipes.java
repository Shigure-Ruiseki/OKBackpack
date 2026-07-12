package ruiseki.okbackpack.common.recipe;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.registry.GameRegistry;
import ruiseki.okbackpack.common.helpers.UpgradeFeatureHelper;
import ruiseki.okbackpack.common.init.OKBackpackBlocks;
import ruiseki.okbackpack.common.init.OKBackpackItems;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.config.ModConfig;
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
                new ItemStack(OKBackpackBlocks.BACKPACK_BASE.get()),
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
                new ItemStack(OKBackpackBlocks.BACKPACK_IRON.get()),
                ModConfig.ironBackpackSlots,
                ModConfig.ironUpgradeSlots,
                "CCC",
                "CBC",
                "CCC",
                'C',
                "ingotIron",
                'B',
                new ItemStack(OKBackpackBlocks.BACKPACK_BASE.get())));

        // Gold Backpack
        GameRegistry.addRecipe(
            new BackpackUpgradeRecipe(
                new ItemStack(OKBackpackBlocks.BACKPACK_GOLD.get()),
                ModConfig.goldBackpackSlots,
                ModConfig.goldUpgradeSlots,
                "CCC",
                "CBC",
                "CCC",
                'C',
                "ingotGold",
                'B',
                new ItemStack(OKBackpackBlocks.BACKPACK_IRON.get())));

        // Diamond Backpack
        GameRegistry.addRecipe(
            new BackpackUpgradeRecipe(
                new ItemStack(OKBackpackBlocks.BACKPACK_DIAMOND.get()),
                ModConfig.diamondBackpackSlots,
                ModConfig.diamondUpgradeSlots,
                "CCC",
                "CBC",
                "CCC",
                'C',
                "gemDiamond",
                'B',
                new ItemStack(OKBackpackBlocks.BACKPACK_GOLD.get())));

        // Obsidian Backpack
        GameRegistry.addRecipe(
            new BackpackUpgradeRecipe(
                new ItemStack(OKBackpackBlocks.BACKPACK_OBSIDIAN.get()),
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
                new ItemStack(OKBackpackBlocks.BACKPACK_DIAMOND.get())));

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
                    new ItemStack(OKBackpackBlocks.BACKPACK_BASE.get()),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);

                recipes.registerDyeRecipes(
                    new ItemStack(OKBackpackBlocks.BACKPACK_IRON.get()),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);

                recipes.registerDyeRecipes(
                    new ItemStack(OKBackpackBlocks.BACKPACK_GOLD.get()),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);

                recipes.registerDyeRecipes(
                    new ItemStack(OKBackpackBlocks.BACKPACK_DIAMOND.get()),
                    accentOre,
                    mainOre,
                    accentColor,
                    mainColor);

                recipes.registerDyeRecipes(
                    new ItemStack(OKBackpackBlocks.BACKPACK_OBSIDIAN.get()),
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
                new ItemStack(OKBackpackItems.ARCANE_CRAFTING_UPGRADE.get()),
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
                new ItemStack(OKBackpackItems.BASE_UPGRADE.get()));
        }

        // Energized Node Upgrade
        ThaumcraftApi.addArcaneCraftingRecipe(
            "VISPOWER",
            new ItemStack(OKBackpackItems.ENERGIZED_NODE_UPGRADE.get()),
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
            new ItemStack(OKBackpackItems.BASE_UPGRADE.get()));
    }

}
