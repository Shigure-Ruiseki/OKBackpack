package ruiseki.okbackpack.compat.nei;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;

import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.GuiOverlayButton;
import codechicken.nei.recipe.IRecipeHandler;

public class BackpackOverlay implements IOverlayHandler {

    @Override
    public void overlayRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, boolean maxTransfer) {
        transferRecipe(firstGui, recipe, recipeIndex, maxTransfer ? Integer.MAX_VALUE : 1);
    }

    @Override
    public int transferRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        return IOverlayHandler.super.transferRecipe(firstGui, recipe, recipeIndex, multiplier);
    }

    @Override
    public boolean canFillCraftingGrid(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex) {
        return IOverlayHandler.super.canFillCraftingGrid(firstGui, recipe, recipeIndex);
    }

    @Override
    public boolean craft(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, int multiplier) {
        return IOverlayHandler.super.craft(firstGui, recipe, recipeIndex, multiplier);
    }

    @Override
    public boolean canCraft(GuiContainer firstGui, IRecipeHandler handler, int recipeIndex) {
        return IOverlayHandler.super.canCraft(firstGui, handler, recipeIndex);
    }

    @Override
    public List<GuiOverlayButton.ItemOverlayState> presenceOverlay(GuiContainer firstGui, IRecipeHandler recipe,
        int recipeIndex) {
        return IOverlayHandler.super.presenceOverlay(firstGui, recipe, recipeIndex);
    }
}
