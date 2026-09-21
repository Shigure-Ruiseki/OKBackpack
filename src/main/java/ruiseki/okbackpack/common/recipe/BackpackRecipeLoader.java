package ruiseki.okbackpack.common.recipe;

import java.util.Collection;

import net.minecraft.util.ResourceLocation;

import com.google.gson.JsonObject;

import ruiseki.okbackpack.OKBackpack;
import ruiseki.okbackpack.Reference;
import ruiseki.okcore.recipe.IRecipeOK;
import ruiseki.okcore.recipe.RecipeManager;
import ruiseki.okcore.recipe.RecipeRegistry;
import ruiseki.okcore.recipe.type.crafting.shaped.ShapedRecipeSerializer;
import ruiseki.okcore.recipe.type.crafting.shaped.ShapedRecipeType;
import ruiseki.okcore.recipe.type.crafting.shapless.ShapelessRecipeSerializer;
import ruiseki.okcore.recipe.type.crafting.shapless.ShapelessRecipeType;

public class BackpackRecipeLoader {

    public static final ResourceLocation SHAPED_TYPE_ID = new ResourceLocation("minecraft", "crafting_shaped");

    public static final ResourceLocation SHAPELESS_TYPE_ID = new ResourceLocation("minecraft", "crafting_shapeless");

    private static boolean registered;

    protected BackpackRecipeLoader() {}

    public static void registerTypes() {
        if (registered) return;
        registered = true;

        RecipeRegistry.registerType(SHAPED_TYPE_ID, ShapedRecipeType.INSTANCE);
        RecipeRegistry.registerSerializer(SHAPED_TYPE_ID, new ShapedRecipeSerializer());

        RecipeRegistry.registerType(SHAPELESS_TYPE_ID, ShapelessRecipeType.INSTANCE);
        RecipeRegistry.registerSerializer(SHAPELESS_TYPE_ID, new ShapelessRecipeSerializer());
    }

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

    public static IRecipeOK<?> parse(ResourceLocation id, JsonObject json) {
        if (json == null || !json.has("type")) return null;

        return RecipeRegistry.deserialize(id, json);
    }

    public static String getRecipeDirectory() {
        return Reference.MOD_ID + "/recipes";
    }

    public static boolean isTypeRegistered(ResourceLocation id) {
        return RecipeRegistry.getType(id) != null;
    }

    public static boolean isSerializerRegistered(ResourceLocation id) {
        return RecipeRegistry.getSerializer(id) != null;
    }
}
