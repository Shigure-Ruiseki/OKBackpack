package ruiseki.okstoragecore.upgrades.cooking;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okcore.item.handler.IItemHandlerSimpleInserter;
import ruiseki.okcore.modcompat.efr.recipe.type.cooking.blast.BlastFurnaceRecipe;
import ruiseki.okcore.modcompat.efr.recipe.type.cooking.blast.RecipeTypeBlastFurnaceConfig;
import ruiseki.okcore.modcompat.efr.recipe.type.cooking.smoker.RecipeTypeSmokerConfig;
import ruiseki.okcore.modcompat.efr.recipe.type.cooking.smoker.SmokerRecipe;
import ruiseki.okcore.recipe.IRecipeType;
import ruiseki.okcore.recipe.RecipeRegistry;
import ruiseki.okcore.recipe.type.cooking.AbstractCookingRecipe;
import ruiseki.okcore.recipe.type.cooking.furnace.FurnaceRecipe;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.helper.RecipeHelpers;
import ruiseki.okstoragecore.upgrades.FilterLogic;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.ItemUpgradeBase;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class AutoCookingUpgradeWrapper<W extends AutoCookingUpgradeWrapper<W, U, R>, U extends ItemUpgradeBase<W> & IAutoCookingUpgradeItem, R extends AbstractCookingRecipe>
    extends UpgradeWrapperBase<W, U> implements ITickableUpgrade, ICookingUpgrade<R> {

    private static final int NOTHING_TO_DO_COOLDOWN = 10;
    private static final int NO_INVENTORY_SPACE_COOLDOWN = 60;

    private final FilterLogic inputFilterLogic;
    private final FilterLogic fuelFilterLogic;
    private final CookingLogic<R> cookingLogic;
    private final Predicate<ItemStack> isValidInput;
    private final Predicate<ItemStack> isValidFuel;
    private final IRecipeType<R> recipeType;
    private int outputCooldown = 0;
    private int fuelCooldown = 0;
    private int inputCooldown = 0;
    private final AutoCookingUpgradeConfig autoCookingUpgradeConfig;

    public AutoCookingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler, IRecipeType<R> recipeType, float burnTimeModifier) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        this.recipeType = recipeType;
        autoCookingUpgradeConfig = upgradeItem.getAutoCookingUpgradeConfig();
        inputFilterLogic = new FilterLogic(
            upgrade,
            upgradeSaveHandler,
            autoCookingUpgradeConfig.getInputFilterSlots(),
            s -> RecipeHelpers.getCookingRecipe(s, recipeType)
                .isPresent(),
            "inputFilter");
        fuelFilterLogic = new FilterLogic(
            upgrade,
            upgradeSaveHandler,
            autoCookingUpgradeConfig.getFuelFilterSlots(),
            s -> TileEntityFurnace.getItemBurnTime(s) > 0,
            "fuelFilter");
        fuelFilterLogic.setAllowByDefault(true);
        fuelFilterLogic.setEmptyAllowListMatchesEverything();

        isValidInput = s -> RecipeHelpers.getCookingRecipe(s, recipeType)
            .isPresent() && inputFilterLogic.matchesFilter(s);
        isValidFuel = s -> TileEntityFurnace.getItemBurnTime(s) > 0 && fuelFilterLogic.matchesFilter(s);
        cookingLogic = new CookingLogic<>(
            upgrade,
            upgradeSaveHandler,
            isValidFuel,
            isValidInput,
            autoCookingUpgradeConfig,
            recipeType,
            burnTimeModifier);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            pauseAndRemoveRenderInfo();
        }
        super.setEnabled(enabled);
    }

    private void pauseAndRemoveRenderInfo() {
        cookingLogic.pause();
    }

    @Override
    public void onBeforeRemoved() {
        pauseAndRemoveRenderInfo();
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (isInCooldown(world)) {
            return;
        }
        tryPushingOutput();
        tryPullingFuel();
        tryPullingInput();

        if (!cookingLogic.tick(world) && outputCooldown <= 0 && fuelCooldown <= 0 && inputCooldown <= 0) {
            setCooldown(world, NOTHING_TO_DO_COOLDOWN);
        }
    }

    private void tryPushingOutput() {
        if (outputCooldown > 0) {
            outputCooldown--;
            return;
        }

        ItemStack output = cookingLogic.getCookOutput();
        IItemHandlerSimpleInserter inventory = storageWrapper.getInventoryForUpgradeProcessing();
        if (!ItemHelpers.isEmpty(output)
            && ItemHelpers.getCount(inventory.insertItem(output, true)) < ItemHelpers.getCount(output)) {
            ItemStack ret = inventory.insertItem(output, false);
            cookingLogic.getCookingInventory()
                .extractItem(
                    CookingLogic.COOK_OUTPUT_SLOT,
                    ItemHelpers.getCount(output) - ItemHelpers.getCount(ret),
                    false);
        } else {
            outputCooldown = NO_INVENTORY_SPACE_COOLDOWN;
        }

        ItemStack fuel = cookingLogic.getFuel();
        if (!ItemHelpers.isEmpty(fuel) && TileEntityFurnace.getItemBurnTime(fuel) <= 0
            && ItemHelpers.getCount(inventory.insertItem(fuel, true)) < ItemHelpers.getCount(fuel)) {
            ItemStack ret = inventory.insertItem(fuel, false);
            cookingLogic.getCookingInventory()
                .extractItem(CookingLogic.FUEL_SLOT, ItemHelpers.getCount(fuel) - ItemHelpers.getCount(ret), false);
        }
    }

    private void tryPullingInput() {
        if (inputCooldown > 0) {
            inputCooldown--;
            return;
        }

        if (tryPullingGetUnsucessful(cookingLogic.getCookInput(), cookingLogic::setCookInput, isValidInput)) {
            inputCooldown = NO_INVENTORY_SPACE_COOLDOWN;
        }
    }

    private void tryPullingFuel() {
        if (fuelCooldown > 0) {
            fuelCooldown--;
            return;
        }

        if (tryPullingGetUnsucessful(cookingLogic.getFuel(), cookingLogic::setFuel, isValidFuel)) {
            fuelCooldown = NO_INVENTORY_SPACE_COOLDOWN;
        }
    }

    private boolean tryPullingGetUnsucessful(ItemStack stack, Consumer<ItemStack> setSlot,
        Predicate<ItemStack> isItemValid) {
        ItemStack toExtract;
        IItemHandlerModifiable inventory = storageWrapper.getInventoryForUpgradeProcessing();
        if (ItemHelpers.isEmpty(stack)) {
            AtomicReference<ItemStack> ret = new AtomicReference<>(ItemHelpers.EMPTY);
            InventoryHelpers.iterate(inventory, (slot, st) -> {
                if (isItemValid.test(st)) {
                    ret.set(st.copy());
                }
            }, () -> !ItemHelpers.isEmpty(ret.get()));
            if (!ItemHelpers.isEmpty(ret.get())) {
                toExtract = ret.get();
                ItemHelpers.setCount(toExtract, toExtract.getMaxStackSize());
            } else {
                return true;
            }
        } else if (ItemHelpers.getCount(stack) == stack.getMaxStackSize() || !isItemValid.test(stack)) {
            return true;
        } else {
            toExtract = stack.copy();
            ItemHelpers.setCount(toExtract, stack.getMaxStackSize() - ItemHelpers.getCount(stack));
        }

        if (ItemHelpers.getCount(InventoryHelpers.extractFromInventory(toExtract, inventory, true)) > 0) {
            ItemStack toSet = InventoryHelpers.extractFromInventory(toExtract, inventory, false);
            ItemHelpers.grow(toSet, ItemHelpers.getCount(stack));
            setSlot.accept(toSet);
        } else {
            return true;
        }
        return false;
    }

    @Override
    public CookingLogic<R> getCookingLogic() {
        return cookingLogic;
    }

    public FilterLogic getInputFilterLogic() {
        return inputFilterLogic;
    }

    public FilterLogic getFuelFilterLogic() {
        return fuelFilterLogic;
    }

    public static class AutoSmeltingUpgradeWrapper
        extends AutoCookingUpgradeWrapper<AutoSmeltingUpgradeWrapper, ItemAutoSmeltingUpgrade, FurnaceRecipe> {

        public AutoSmeltingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            super(storageWrapper, upgrade, upgradeSaveHandler, RecipeRegistry.SMELTING, 1);
        }
    }

    public static class AutoBlastingUpgradeWrapper
        extends AutoCookingUpgradeWrapper<AutoBlastingUpgradeWrapper, ItemAutoBlastingUpgrade, BlastFurnaceRecipe> {

        public AutoBlastingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            super(
                storageWrapper,
                upgrade,
                upgradeSaveHandler,
                RecipeTypeBlastFurnaceConfig._instance.getInstance(),
                0.5f);
        }
    }

    public static class AutoSmokingUpgradeWrapper
        extends AutoCookingUpgradeWrapper<AutoSmokingUpgradeWrapper, ItemAutoSmokingUpgrade, SmokerRecipe> {

        public AutoSmokingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
            Consumer<ItemStack> upgradeSaveHandler) {
            super(storageWrapper, upgrade, upgradeSaveHandler, RecipeTypeSmokerConfig._instance.getInstance(), 0.5f);
        }
    }
}
