package ruiseki.okstoragecore.upgrades.xppump;

import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.block.collidable.ImmutableAxisAlignedBB;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.helper.PlayerHelpers;
import ruiseki.okstoragecore.api.IStorageFluidHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.helper.XpHelpers;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class XpPumpUpgradeWrapper extends UpgradeWrapperBase<XpPumpUpgradeWrapper, ItemXpPumpUpgrade>
    implements ITickableUpgrade {

    private static final int DEFAULT_LEVEL = 10;
    private static final int COOLDOWN = 5;
    private static final int ALL_LEVELS = 10000;
    private static final int PLAYER_SEARCH_RANGE = 3;

    private final XpPumpUpgradeConfig xpPumpUpgradeConfig;

    protected XpPumpUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        xpPumpUpgradeConfig = upgradeItem.getXpPumpUpgradeConfig();
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if ((entity != null && !(entity instanceof EntityPlayer)) || isInCooldown(world)) {
            return;
        }

        if (entity == null) {
            ImmutableAxisAlignedBB searchBox = ImmutableAxisAlignedBB.fromBounds(
                pos.getX() - PLAYER_SEARCH_RANGE,
                pos.getY() - PLAYER_SEARCH_RANGE,
                pos.getZ() - PLAYER_SEARCH_RANGE,
                pos.getX() + 1 + PLAYER_SEARCH_RANGE,
                pos.getY() + 1 + PLAYER_SEARCH_RANGE,
                pos.getZ() + 1 + PLAYER_SEARCH_RANGE);

            for (EntityPlayer player : world.playerEntities) {

                if (searchBox.isVecInside(Vec3.createVectorHelper(player.posX, player.posY, player.posZ))) {
                    interactWithPlayer(player);
                    mendItems(player);
                }
            }
        } else {
            EntityPlayer player = (EntityPlayer) entity;
            interactWithPlayer(player);
            mendItems(player);
        }

        setCooldown(world, COOLDOWN);
    }

    private void mendItems(EntityPlayer player) {
        if (!xpPumpUpgradeConfig.isMendingOn() || !shouldMendItems()) {
            return;
        }

        ItemStack[] candidates = new ItemStack[] { player.getCurrentEquippedItem(), player.getEquipmentInSlot(1),
            player.getEquipmentInSlot(2), player.getEquipmentInSlot(3), player.getEquipmentInSlot(4) };

        for (ItemStack stack : candidates) {
            if (ItemHelpers.isEmpty(stack) || stack.getItemDamage() <= 0
                || !ItemHelpers.getItem(stack)
                    .isRepairable()
                || XpHelpers.getMendingLevel(stack) <= 0) {
                continue;
            }

            FluidStack contents = storageWrapper.getFluidHandler()
                .map(handler -> handler.drain(XpHelpers.getXpJuiceFluid(), Integer.MAX_VALUE, false, false))
                .orElse(FluidHelpers.EMPTY);

            int availableMb = FluidHelpers.getAmount(contents);
            if (availableMb < XpHelpers.RATIO) break;

            int xpAvailable = availableMb / XpHelpers.RATIO;
            int xpNeeded = (stack.getItemDamage() + 1) / 2;

            int maxXp = xpPumpUpgradeConfig.getMaxXpPointsPerMending();
            int xpToDrain = Math.min(xpNeeded, Math.min(xpAvailable, maxXp));
            if (xpToDrain <= 0) continue;

            FluidStack drained = storageWrapper.getFluidHandler()
                .map(handler -> handler.drain(XpHelpers.getXpJuiceFluid(), xpToDrain * XpHelpers.RATIO, true, false))
                .orElse(FluidHelpers.EMPTY);

            int xpDrained = FluidHelpers.getAmount(drained) / XpHelpers.RATIO;
            if (xpDrained <= 0) break;

            int durabilityToRepair = xpDrained * 2;
            stack.setItemDamage(Math.max(0, stack.getItemDamage() - durabilityToRepair));
        }
    }

    private void interactWithPlayer(EntityPlayer player) {
        storageWrapper.getFluidHandler()
            .ifPresent(fluidHandler -> {
                int level = getLevel();
                AutomationDirection direction = getDirection();
                if (direction == AutomationDirection.OFF) {
                    return;
                }

                if (direction == AutomationDirection.INPUT) {
                    if (level < player.experienceLevel || (level == player.experienceLevel && player.experience > 0)) {
                        tryFillTankWithPlayerExperience(player, fluidHandler, level, false);
                    }
                } else if (direction == AutomationDirection.OUTPUT && level > player.experienceLevel) {
                    tryGivePlayerExperienceFromTank(player, fluidHandler, level, false);
                }
            });
    }

    private void tryGivePlayerExperienceFromTank(EntityPlayer player, IStorageFluidHandler fluidHandler,
        int stopAtLevel) {
        tryGivePlayerExperienceFromTank(player, fluidHandler, stopAtLevel, true);
    }

    private void tryGivePlayerExperienceFromTank(EntityPlayer player, IStorageFluidHandler fluidHandler,
        int stopAtLevel, boolean ignoreInOutLimit) {
        int maxXpPointsToGive = XpHelpers.getExperienceForLevel(stopAtLevel)
            - XpHelpers.getPlayerTotalExperience(player);
        FluidStack drained = fluidHandler.drain(
            XpHelpers.getXpJuiceFluid(),
            XpHelpers.experienceToLiquid(maxXpPointsToGive),
            true,
            ignoreInOutLimit);
        if (!FluidHelpers.isEmpty(drained)) {
            PlayerHelpers
                .giveExperiencePoints(player, (int) XpHelpers.liquidToExperience(FluidHelpers.getAmount(drained)));
        }
    }

    private void tryFillTankWithPlayerExperience(EntityPlayer player, IStorageFluidHandler fluidHandler,
        int stopAtLevel) {
        tryFillTankWithPlayerExperience(player, fluidHandler, stopAtLevel, true);
    }

    private void tryFillTankWithPlayerExperience(EntityPlayer player, IStorageFluidHandler fluidHandler,
        int stopAtLevel, boolean ignoreInOutLimit) {
        int maxXpPointsToTake = XpHelpers.getPlayerTotalExperience(player)
            - XpHelpers.getExperienceForLevel(stopAtLevel);
        int filled = fluidHandler.fill(
            new FluidStack(XpHelpers.getXpJuiceFluid(), XpHelpers.experienceToLiquid(maxXpPointsToTake)),
            true,
            ignoreInOutLimit);
        if (filled > 0) {
            PlayerHelpers.giveExperiencePoints(player, (int) -XpHelpers.liquidToExperience(filled));
        }
    }

    public void takeLevelsFromPlayer(EntityPlayer player) {
        storageWrapper.getFluidHandler()
            .ifPresent(
                fluidHandler -> tryFillTankWithPlayerExperience(
                    player,
                    fluidHandler,
                    Math.max(player.experienceLevel - getLevelsToStore(), 0)));
    }

    public void takeAllExperienceFromPlayer(EntityPlayer player) {
        storageWrapper.getFluidHandler()
            .ifPresent(fluidHandler -> tryFillTankWithPlayerExperience(player, fluidHandler, 0));
    }

    public void giveLevelsToPlayer(EntityPlayer player) {
        storageWrapper.getFluidHandler()
            .ifPresent(
                fluidHandler -> tryGivePlayerExperienceFromTank(
                    player,
                    fluidHandler,
                    player.experienceLevel + getLevelsToTake()));
    }

    public void giveAllExperienceToPlayer(EntityPlayer player) {
        storageWrapper.getFluidHandler()
            .ifPresent(fluidHandler -> tryGivePlayerExperienceFromTank(player, fluidHandler, ALL_LEVELS));
    }

    public AutomationDirection getDirection() {
        return NBTHelpers.getEnumConstant(upgrade, "direction", AutomationDirection::fromName)
            .orElse(AutomationDirection.INPUT);
    }

    public void setDirection(AutomationDirection direction) {
        NBTHelpers.setEnumConstant(upgrade, "direction", direction);
        save();
    }

    public void setLevel(int level) {
        NBTHelpers.setInteger(upgrade, "level", level);
        save();
    }

    public int getLevel() {
        return NBTHelpers.getInteger(upgrade, "level")
            .orElse(DEFAULT_LEVEL);
    }

    public void setLevelsToStore(int levelsToTake) {
        NBTHelpers.setInteger(upgrade, "levelsToStore", levelsToTake);
        save();
    }

    public int getLevelsToStore() {
        return NBTHelpers.getInteger(upgrade, "levelsToStore")
            .orElse(1);
    }

    public void setLevelsToTake(int levelsToGive) {
        NBTHelpers.setInteger(upgrade, "levelsToTake", levelsToGive);
        save();
    }

    public int getLevelsToTake() {
        return NBTHelpers.getInteger(upgrade, "levelsToTake")
            .orElse(1);
    }

    public boolean shouldMendItems() {
        return NBTHelpers.getBoolean(upgrade, "mendItems")
            .orElse(true);
    }

    public void setMendItems(boolean mendItems) {
        NBTHelpers.setBoolean(upgrade, "mendItems", mendItems);
        save();
    }
}
