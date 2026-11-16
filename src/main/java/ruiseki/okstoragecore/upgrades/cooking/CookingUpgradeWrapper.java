package ruiseki.okstoragecore.upgrades.cooking;

import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.recipe.IRecipeType;
import ruiseki.okcore.recipe.RecipeRegistry;
import ruiseki.okcore.recipe.type.cooking.AbstractCookingRecipe;
import ruiseki.okcore.recipe.type.cooking.furnace.FurnaceRecipe;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public abstract class CookingUpgradeWrapper<W extends CookingUpgradeWrapper<W, U, R>, U extends ItemUpgradeBase<W> & ICookingUpgradeItem, R extends AbstractCookingRecipe>
    extends UpgradeWrapperBase<W, U> implements ITickableUpgrade, ICookingUpgrade<R> {

    private static final int NOTHING_TO_DO_COOLDOWN = 10;
    protected final CookingLogic<R> cookingLogic;

    protected CookingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler, IRecipeType<R> recipeType, float burnTimeModifier) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        cookingLogic = new CookingLogic<>(
            upgrade,
            upgradeSaveHandler,
            upgradeItem.getCookingUpgradeConfig(),
            recipeType,
            burnTimeModifier);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World level, BlockPos pos) {
        if (isInCooldown(level)) return;
        if (!cookingLogic.tick(level)) {
            setCooldown(level, NOTHING_TO_DO_COOLDOWN);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            pauseAndRemoveRenderInfo();
        }
        super.setEnabled(enabled);
    }

    @Override
    public void onBeforeRemoved() {
        pauseAndRemoveRenderInfo();
    }

    private void pauseAndRemoveRenderInfo() {
        cookingLogic.pause();
    }

    public CookingLogic<R> getCookingLogic() {
        return cookingLogic;
    }

    public static class SmeltingUpgradeWrapper
        extends CookingUpgradeWrapper<SmeltingUpgradeWrapper, ItemSmeltingUpgrade, FurnaceRecipe> {

        public SmeltingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            super(storageWrapper, upgrade, upgradeSaveHandler, RecipeRegistry.SMELTING, 1);
        }
    }

}
