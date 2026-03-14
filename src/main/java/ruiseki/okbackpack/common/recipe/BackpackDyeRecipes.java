package ruiseki.okbackpack.common.recipe;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.registry.GameRegistry;

public class BackpackDyeRecipes {

    public BackpackDyeRecipes() {}

    public void registerDyeRecipes(ItemStack baseBackpack, String accentOreName, String mainOreName, int accentColor,
        int mainColor) {

        // Main color
        GameRegistry.addRecipe(
            new BackpackDyeRecipe(
                baseBackpack.copy(),
                mainColor,
                -1,
                "   ",
                " BM",
                "   ",
                'B',
                baseBackpack,
                'M',
                mainOreName));

        // Accent color
        GameRegistry.addRecipe(
            new BackpackDyeRecipe(
                baseBackpack.copy(),
                -1,
                accentColor,
                "   ",
                " B ",
                " A ",
                'B',
                baseBackpack,
                'A',
                accentOreName));

        // Both colors
        GameRegistry.addRecipe(
            new BackpackDyeRecipe(
                baseBackpack.copy(),
                mainColor,
                accentColor,
                "   ",
                " BM",
                " A ",
                'B',
                baseBackpack,
                'A',
                accentOreName,
                'M',
                mainOreName));
    }
}
