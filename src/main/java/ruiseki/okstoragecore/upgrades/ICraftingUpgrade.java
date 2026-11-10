package ruiseki.okstoragecore.upgrades;

public interface ICraftingUpgrade {

    int transferRecipe(RecipeTransferContext context, int multiplier);
}
