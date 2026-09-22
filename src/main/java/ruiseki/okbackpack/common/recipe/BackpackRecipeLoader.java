package ruiseki.okbackpack.common.recipe;

import java.util.Collection;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okcore.recipe.IRecipeOK;
import ruiseki.okcore.recipe.RecipeManager;
import ruiseki.okcore.recipe.RecipeRegistry;

// TODO: requires OKCore to fix asynchronous loading and server side recipe registration
@Deprecated
public class BackpackRecipeLoader {

    public static int publishRecipes() {
        RecipeManager manager = RecipeManager.getManager();
        if (manager == null) {
            OKBackpack.okLog("Skipped registering backpack recipes: no recipe manager available");
            return 0;
        }

        RecipeRegistry.syncMCCraftingManager();

        Collection<IRecipeOK<?>> recipes = manager.getRecipes();
        int count = recipes != null ? recipes.size() : 0;

        if (count == 0) {
            OKBackpack.okLog("No backpack crafting recipes were loaded");
        } else {
            OKBackpack.okLog("Registered " + count + " backpack crafting recipes");
        }

        return count;
    }
}
