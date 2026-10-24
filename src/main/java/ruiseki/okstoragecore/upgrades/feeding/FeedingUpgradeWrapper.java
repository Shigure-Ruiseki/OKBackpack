
package ruiseki.okstoragecore.upgrades.feeding;

import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.item.handler.IItemHandlerModifiable;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.FilterLogic;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class FeedingUpgradeWrapper extends UpgradeWrapperBase<FeedingUpgradeWrapper, ItemFeedingUpgrade>
    implements ITickableUpgrade, IFilteredUpgrade {

    private static final int COOLDOWN = 100;
    private static final int STILL_HUNGRY_COOLDOWN = 10;
    private static final int FEEDING_RANGE = 3;

    private final FilterLogic filterLogic;

    protected FeedingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);

        filterLogic = new FilterLogic(
            upgrade,
            upgradeSaveHandler,
            upgradeItem.getFilterSlotCount(),
            FeedingUpgradeWrapper::isEdible);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (world.isRemote || isInCooldown(world)) {
            return;
        }

        if (entity != null && !(entity instanceof EntityPlayer)) {
            return;
        }

        boolean hungryPlayer = false;

        if (entity instanceof EntityPlayer) {
            hungryPlayer = feedPlayerAndGetHungry((EntityPlayer) entity, world);
        } else {
            double x = pos.getX();
            double y = pos.getY();
            double z = pos.getZ();

            net.minecraft.util.AxisAlignedBB area = net.minecraft.util.AxisAlignedBB.getBoundingBox(
                x - FEEDING_RANGE,
                y - FEEDING_RANGE,
                z - FEEDING_RANGE,
                x + 1 + FEEDING_RANGE,
                y + 1 + FEEDING_RANGE,
                z + 1 + FEEDING_RANGE);

            for (Object object : world.getEntitiesWithinAABB(EntityPlayer.class, area)) {

                if (feedPlayerAndGetHungry((EntityPlayer) object, world)) {
                    hungryPlayer = true;
                }
            }
        }

        setCooldown(world, hungryPlayer ? STILL_HUNGRY_COOLDOWN : COOLDOWN);
    }

    private boolean feedPlayerAndGetHungry(EntityPlayer player, World world) {
        int hungerLevel = 20 - player.getFoodStats()
            .getFoodLevel();
        if (hungerLevel <= 0) return false;
        return tryFeedingFoodFromStorage(world, hungerLevel, player) && player.getFoodStats()
            .getFoodLevel() < 20;
    }

    private boolean tryFeedingFoodFromStorage(World world, int hungerLevel, EntityPlayer player) {
        boolean isHurt = player.getHealth() < player.getMaxHealth() - 0.1F;

        IItemHandlerModifiable inventory = storageWrapper.getInventoryForUpgradeProcessing();
        final boolean[] fedPlayer = { false };

        InventoryHelpers.iterate(inventory, (slot, stack) -> {
            if (!isEdible(stack) || !filterLogic.matchesFilter(stack)) {
                return false;
            }

            if (!isHungryEnoughForFood(hungerLevel, stack, player)
                && !(shouldFeedImmediatelyWhenHurt() && hungerLevel > 0 && isHurt)) {
                return false;
            }

            ItemFood food = (ItemFood) stack.getItem();

            // Keep the stored stack untouched until consumption has been processed.
            ItemStack foodStack = stack.copy();
            ItemStack beforeEating = foodStack.copy();

            // Consume the food and obtain the resulting stack.
            ItemStack result = food.onEaten(foodStack, world, player);

            // Apply Forge's finish-use event to the resulting stack.
            result = ForgeEventFactory.onItemUseFinish(player, beforeEating, 0, result);

            if (result != null && result.stackSize <= 0) {
                result = null;
            }

            // Save the resulting stack back into storage.
            inventory.setStackInSlot(slot, result);

            fedPlayer[0] = true;
            return true;
        }, () -> false, ret -> ret);

        return fedPlayer[0];
    }

    private static boolean isEdible(@Nullable ItemStack stack) {
        return stack != null && stack.stackSize > 0
            && stack.getItem() instanceof ItemFood
            && ((ItemFood) stack.getItem()).func_150905_g(stack) >= 1;
    }

    private boolean isHungryEnoughForFood(int hungerLevel, ItemStack stack, EntityPlayer player) {
        if (!isEdible(stack)) {
            return false;
        }

        HungerLevel feedAtHungerLevel = getFeedAtHungerLevel();
        if (feedAtHungerLevel == HungerLevel.ANY) {
            return true;
        }

        int nutrition = ((ItemFood) stack.getItem()).func_150905_g(stack);
        return (feedAtHungerLevel == HungerLevel.HALF ? nutrition / 2 : nutrition) <= hungerLevel;
    }

    @Override
    public FilterLogic getFilterLogic() {
        return filterLogic;
    }

    public HungerLevel getFeedAtHungerLevel() {
        return NBTHelpers.getEnumConstant(upgrade, "feedAtHungerLevel", HungerLevel::fromName)
            .orElse(HungerLevel.HALF);
    }

    public void setFeedAtHungerLevel(HungerLevel hungerLevel) {
        NBTHelpers.setEnumConstant(upgrade, "feedAtHungerLevel", hungerLevel);
        save();
    }

    public boolean shouldFeedImmediatelyWhenHurt() {
        return NBTHelpers.getBoolean(upgrade, "feedImmediatelyWhenHurt")
            .orElse(true);
    }

    public void setFeedImmediatelyWhenHurt(boolean value) {
        NBTHelpers.setBoolean(upgrade, "feedImmediatelyWhenHurt", value);
        save();
    }
}
