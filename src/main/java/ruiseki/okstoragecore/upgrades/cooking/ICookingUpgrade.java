package ruiseki.okstoragecore.upgrades.cooking;

import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcore.recipe.type.cooking.AbstractCookingRecipe;
import ruiseki.okstoragecore.upgrades.UpgradeGroup;

public interface ICookingUpgrade<T extends AbstractCookingRecipe> {

    UpgradeGroup UPGRADE_GROUP = new UpgradeGroup("furnace_upgrades", LangHelpers.localize("cooking_upgrades"));

    CookingLogic<T> getCookingLogic();
}
