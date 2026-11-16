package ruiseki.okstoragecore.upgrades.cooking;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okcore.recipe.IRecipeOK;
import ruiseki.okcore.recipe.IRecipeType;
import ruiseki.okcore.recipe.type.cooking.AbstractCookingRecipe;
import ruiseki.okstoragecore.helper.RecipeHelpers;

public class CookingLogic<T extends AbstractCookingRecipe> {

    private final ItemStack upgrade;
    private final Consumer<ItemStack> saveHandler;

    private ItemStackHandler cookingInventory = null;
    public static final int COOK_INPUT_SLOT = 0;
    public static final int COOK_OUTPUT_SLOT = 2;
    public static final int FUEL_SLOT = 1;
    @Nullable
    private T cookingRecipe = null;
    private boolean cookingRecipeInitialized = false;

    private final float burnTimeModifier;
    private final Predicate<ItemStack> isFuel;
    private final Predicate<ItemStack> isInput;
    private final double cookingSpeedMultiplier;
    private final double fuelEfficiencyMultiplier;
    private final IRecipeType<T> recipeType;

    private boolean paused = false;
    private long remainingCookTime = 0;
    private long remainingBurnTime = 0;

    public CookingLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, CookingUpgradeConfig cookingUpgradeConfig,
        IRecipeType<T> recipeType, float burnTimeModifier) {
        this(
            upgrade,
            saveHandler,
            s -> getBurnTime(s, recipeType, burnTimeModifier) > 0,
            s -> RecipeHelpers.getCookingRecipe(s, recipeType)
                .isPresent(),
            cookingUpgradeConfig,
            recipeType,
            burnTimeModifier);
    }

    public CookingLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, Predicate<ItemStack> isFuel,
        Predicate<ItemStack> isInput, CookingUpgradeConfig cookingUpgradeConfig, IRecipeType<T> recipeType,
        float burnTimeModifier) {
        this.upgrade = upgrade;
        this.saveHandler = saveHandler;
        this.isFuel = isFuel;
        this.isInput = isInput;
        cookingSpeedMultiplier = cookingUpgradeConfig.getCookingSpeedMultiplier();
        fuelEfficiencyMultiplier = cookingUpgradeConfig.getFuelEfficiencyMultiplier();
        this.recipeType = recipeType;
        this.burnTimeModifier = burnTimeModifier;
    }

    private void save() {
        saveHandler.accept(upgrade);
    }

    public boolean tick(World world) {
        updateTimes(world);

        AtomicBoolean didSomething = new AtomicBoolean(true);
        if (isBurning(world) || readyToStartCooking()) {
            Optional<T> fr = getCookingRecipe();
            if (fr.isEmpty() && isCooking()) {
                setIsCooking(false);
            }
            fr.ifPresent(recipe -> {
                updateFuel(world, recipe);

                if (isBurning(world) && canSmelt(recipe)) {
                    updateCookingProgress(world, recipe);
                } else if (!isBurning(world)) {
                    didSomething.set(false);
                }
            });
        }

        if (!isBurning(world) && isCooking()) {
            updateCookingCooldown(world);
        } else {
            didSomething.set(false);
        }
        return didSomething.get();
    }

    private void updateTimes(World world) {
        if (paused) {
            unpause(world);
            return;
        }

        if (isBurning(world)) {
            remainingBurnTime = getBurnTimeFinish() - world.getTotalWorldTime();
        } else {
            remainingBurnTime = 0;
        }
        if (isCooking()) {
            remainingCookTime = getCookTimeFinish() - world.getTotalWorldTime();
        } else {
            remainingCookTime = 0;
        }
    }

    private void unpause(World world) {
        paused = false;

        if (remainingBurnTime > 0) {
            setBurnTimeFinish(world.getTotalWorldTime() + remainingBurnTime);
        }
        if (remainingCookTime > 0) {
            setCookTimeFinish(world.getTotalWorldTime() + remainingCookTime);
            setIsCooking(true);
        }
    }

    public boolean isBurning(World world) {
        return getBurnTimeFinish() >= world.getTotalWorldTime();
    }

    private Optional<T> getCookingRecipe() {
        if (!cookingRecipeInitialized) {
            cookingRecipe = RecipeHelpers.getCookingRecipe(getCookInput(), recipeType)
                .orElse(null);
            cookingRecipeInitialized = true;
        }
        return Optional.ofNullable(cookingRecipe);
    }

    private void updateCookingCooldown(World world) {
        if (getRemainingCookTime(world) + 2 > getCookTimeTotal()) {
            setIsCooking(false);
        } else {
            setCookTimeFinish(
                world.getTotalWorldTime() + Math.min(getRemainingCookTime(world) + 2, getCookTimeTotal()));
        }
    }

    private void updateCookingProgress(World world, T cookingRecipe) {
        if (isCooking() && finishedCooking(world)) {
            smelt(cookingRecipe);
            if (canSmelt(cookingRecipe)) {
                setCookTime(world, (int) (cookingRecipe.getCookingTime() * (1 / cookingSpeedMultiplier)));
            } else {
                setIsCooking(false);
            }
        } else if (!isCooking()) {
            setIsCooking(true);
            setCookTime(world, (int) (cookingRecipe.getCookingTime() * (1 / cookingSpeedMultiplier)));
        }
    }

    private boolean finishedCooking(World world) {
        return getCookTimeFinish() <= world.getTotalWorldTime();
    }

    private boolean readyToStartCooking() {
        return !ItemHelpers.isEmpty(getFuel()) && !ItemHelpers.isEmpty(getCookInput());
    }

    private void smelt(IRecipeOK<?> recipe) {
        if (!canSmelt(recipe)) {
            return;
        }

        ItemStack input = getCookInput();
        ItemStack recipeOutput = recipe.getResultItem();
        ItemStack output = getCookOutput();
        if (ItemHelpers.isEmpty(output)) {
            setCookOutput(recipeOutput.copy());
        } else if (output.getItem() == recipeOutput.getItem()) {
            ItemHelpers.grow(output, ItemHelpers.getCount(recipeOutput));
            setCookOutput(output);
        }

        if (!ItemHelpers.isEmpty(getFuel()) && getFuel().getItem() == Items.bucket) {
            setFuel(new ItemStack(Items.water_bucket));
        }

        ItemHelpers.shrink(input, 1);
        setCookInput(input);
    }

    public void setCookInput(ItemStack input) {
        cookingInventory.setStackInSlot(COOK_INPUT_SLOT, input);
    }

    private void setCookOutput(ItemStack stack) {
        getCookingInventory().setStackInSlot(COOK_OUTPUT_SLOT, stack);
    }

    private int getRemainingCookTime(World world) {
        return (int) (getCookTimeFinish() - world.getTotalWorldTime());
    }

    private void setCookTime(World world, int cookTime) {
        setCookTimeFinish(world.getTotalWorldTime() + cookTime);
        setCookTimeTotal(cookTime);
    }

    public void pause() {
        paused = true;
        setCookTimeFinish(0);
        setIsCooking(false);
        setBurnTimeFinish(0);
    }

    private void updateFuel(World world, T cookingRecipe) {
        ItemStack fuel = getFuel();
        if (!isBurning(world) && canSmelt(cookingRecipe)) {
            if (getBurnTime(fuel, recipeType, burnTimeModifier) <= 0) {
                return;
            }
            setBurnTime(
                world,
                (int) (getBurnTime(fuel, recipeType, burnTimeModifier) * fuelEfficiencyMultiplier
                    / cookingSpeedMultiplier));
            if (isBurning(world)) {
                if (ItemHelpers.hasContainerItem(fuel)) {
                    setFuel(ItemHelpers.getContainerItem(fuel));
                } else if (!ItemHelpers.isEmpty(fuel)) {
                    ItemHelpers.shrink(fuel, 1);
                    setFuel(fuel);
                    if (ItemHelpers.isEmpty(fuel)) {
                        setFuel(ItemHelpers.getContainerItem(fuel));
                    }
                }
            }
        }
    }

    private void setBurnTime(World world, int burnTime) {
        setBurnTimeFinish(world.getTotalWorldTime() + burnTime);
        setBurnTimeTotal(burnTime);
    }

    protected boolean canSmelt(IRecipeOK<?> cookingRecipe) {
        if (ItemHelpers.isEmpty(getCookInput())) {
            return false;
        }
        ItemStack recipeOutput = cookingRecipe.getResultItem();
        if (ItemHelpers.isEmpty(recipeOutput)) {
            return false;
        } else {
            ItemStack output = getCookOutput();
            if (ItemHelpers.isEmpty(output)) {
                return true;
            } else if (!ItemHelpers.areItemsEqual(output, recipeOutput)) {
                return false;
            } else if (ItemHelpers.getCount(output) + ItemHelpers.getCount(recipeOutput) <= 64
                && ItemHelpers.getCount(output) + ItemHelpers.getCount(recipeOutput) <= output.getMaxStackSize()) {
                    return true;
                } else {
                    return ItemHelpers.getCount(output) + ItemHelpers.getCount(recipeOutput)
                        <= recipeOutput.getMaxStackSize();
                }
        }
    }

    private static <T extends AbstractCookingRecipe> int getBurnTime(ItemStack fuel, IRecipeType<T> recipeType,
        float burnTimeModifier) {
        return (int) (TileEntityFurnace.getItemBurnTime(fuel) * burnTimeModifier);
    }

    public ItemStack getCookOutput() {
        return getCookingInventory().getStackInSlot(COOK_OUTPUT_SLOT);
    }

    public ItemStack getCookInput() {
        return getCookingInventory().getStackInSlot(COOK_INPUT_SLOT);
    }

    public ItemStack getFuel() {
        return getCookingInventory().getStackInSlot(FUEL_SLOT);
    }

    public void setFuel(ItemStack fuel) {
        getCookingInventory().setStackInSlot(FUEL_SLOT, fuel);
    }

    public ItemStackHandler getCookingInventory() {
        if (cookingInventory == null) {
            cookingInventory = new ItemStackHandler(3) {

                @Override
                protected void onContentsChanged(int slot) {
                    super.onContentsChanged(slot);
                    NBTHelpers.setCompoundTag(upgrade, "cookingInventory", this.serializeNBT());
                    save();
                    if (slot == COOK_INPUT_SLOT) {
                        cookingRecipeInitialized = false;
                    }
                }

                @Override
                public boolean isItemValid(int slot, ItemStack stack) {
                    return switch (slot) {
                        case COOK_INPUT_SLOT -> isInput.test(stack);
                        case FUEL_SLOT -> isFuel.test(stack);
                        default -> true;
                    };
                }
            };

            NBTHelpers.getCompoundTag(upgrade, "smeltingInventory")
                .ifPresentOrElse(
                    cookingInventory::deserializeNBT,
                    () -> NBTHelpers.getCompoundTag(upgrade, "cookingInventory")
                        .ifPresent(cookingInventory::deserializeNBT));
        }
        return cookingInventory;
    }

    public long getBurnTimeFinish() {
        return NBTHelpers.getLong(upgrade, "burnTimeFinish")
            .orElse(0L);
    }

    private void setBurnTimeFinish(long burnTimeFinish) {
        NBTHelpers.setLong(upgrade, "burnTimeFinish", burnTimeFinish);
        save();
    }

    public int getBurnTimeTotal() {
        return NBTHelpers.getInteger(upgrade, "burnTimeTotal")
            .orElse(0);
    }

    private void setBurnTimeTotal(int burnTimeTotal) {
        NBTHelpers.setInteger(upgrade, "burnTimeTotal", burnTimeTotal);
        save();
    }

    public long getCookTimeFinish() {
        return NBTHelpers.getLong(upgrade, "cookTimeFinish")
            .orElse(-1L);
    }

    private void setCookTimeFinish(long cookTimeFinish) {
        NBTHelpers.setLong(upgrade, "cookTimeFinish", cookTimeFinish);
        save();
    }

    public int getCookTimeTotal() {
        return NBTHelpers.getInteger(upgrade, "cookTimeTotal")
            .orElse(0);
    }

    private void setCookTimeTotal(int cookTimeTotal) {
        NBTHelpers.setInteger(upgrade, "cookTimeTotal", cookTimeTotal);
        save();
    }

    public boolean isCooking() {
        return NBTHelpers.getBoolean(upgrade, "isCooking")
            .orElse(false);
    }

    private void setIsCooking(boolean isCooking) {
        NBTHelpers.setBoolean(upgrade, "isCooking", isCooking);
        save();
    }
}
