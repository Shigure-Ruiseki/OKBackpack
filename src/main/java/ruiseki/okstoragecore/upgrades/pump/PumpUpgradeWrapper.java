package ruiseki.okstoragecore.upgrades.pump;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.block.collidable.ImmutableAxisAlignedBB;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.fluid.capability.CapabilityFluidHandler;
import ruiseki.okcore.fluid.capability.wrapper.BlockLiquidWrapper;
import ruiseki.okcore.fluid.capability.wrapper.FluidBlockWrapper;
import ruiseki.okcore.fluid.handler.IFluidHandler;
import ruiseki.okcore.fluid.handler.IFluidHandlerItem;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.helper.TileHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.FluidFilterLogic;
import ruiseki.okstoragecore.upgrades.IFluidFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class PumpUpgradeWrapper extends UpgradeWrapperBase<PumpUpgradeWrapper, ItemPumpUpgrade>
    implements ITickableUpgrade, IFluidFilteredUpgrade {

    private static final int DID_NOTHING_COOLDOWN_TIME = 40;
    private static final int HAND_INTERACTION_COOLDOWN_TIME = 3;
    private static final int WORLD_INTERACTION_COOLDOWN_TIME = 20;
    private static final int FLUID_HANDLER_INTERACTION_COOLDOWN_TIME = 20;
    private static final int PLAYER_SEARCH_RANGE = 3;
    private static final int PUMP_IN_WORLD_RANGE = 4;
    private static final int PUMP_IN_WORLD_RANGE_SQR = PUMP_IN_WORLD_RANGE * PUMP_IN_WORLD_RANGE;

    private long lastHandActionTime = -1;
    private final FluidFilterLogic fluidFilterLogic;
    private final int slotCount;
    private final PumpUpgradeConfig pumpUpgradeConfig;

    protected PumpUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        this.pumpUpgradeConfig = this.upgradeItem.getPumpUpgradeConfig();
        this.slotCount = pumpUpgradeConfig.getFilterSlots();
        this.fluidFilterLogic = new FluidFilterLogic(
            this.upgrade,
            upgradeSaveHandler,
            this.pumpUpgradeConfig.getFilterSlots());
    }

    public int getSlotCount() {
        return slotCount;
    }

    @Override
    public FluidFilterLogic getFilterLogic() {
        return this.fluidFilterLogic;
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (isInCooldown(world)) return;
        setCooldown(
            world,
            storageWrapper.getFluidHandler()
                .map(storageFluidHandler -> tick(storageFluidHandler, entity, world, pos))
                .orElse(DID_NOTHING_COOLDOWN_TIME));
    }

    private int tick(IFluidHandlerItem storageFluidHandler, @Nullable EntityLivingBase entity, World level,
        BlockPos pos) {
        if (entity == null) {
            Optional<Integer> newCooldown = handleInWorldInteractions(storageFluidHandler, level, pos);
            if (newCooldown.isPresent()) {
                return newCooldown.get();
            }
        } else {
            if (shouldInteractWithHand() && entity instanceof EntityPlayer player
                && handleFluidContainerInHands(player, storageFluidHandler)) {
                lastHandActionTime = level.getTotalWorldTime();
                return HAND_INTERACTION_COOLDOWN_TIME;
            }
            Optional<Integer> newCooldown = handleInWorldInteractions(storageFluidHandler, level, pos);
            if (newCooldown.isPresent()) {
                return newCooldown.get();
            }
        }
        return lastHandActionTime + 10 * HAND_INTERACTION_COOLDOWN_TIME > level.getTotalWorldTime()
            ? HAND_INTERACTION_COOLDOWN_TIME
            : DID_NOTHING_COOLDOWN_TIME;
    }

    private Optional<Integer> handleInWorldInteractions(IFluidHandlerItem storageFluidHandler, World world,
        BlockPos pos) {
        if (shouldInteractWithHand() && handleFluidContainersInHandsOfNearbyPlayers(world, pos, storageFluidHandler)) {
            lastHandActionTime = world.getTotalWorldTime();
            return Optional.of(HAND_INTERACTION_COOLDOWN_TIME);
        }

        if (shouldInteractWithWorld()) {
            Optional<Integer> newCooldown = interactWithWorld(world, pos, storageFluidHandler);
            if (newCooldown.isPresent()) {
                return newCooldown;
            }
        }

        return interactWithAttachedFluidHandlers(world, pos, storageFluidHandler);
    }

    private Optional<Integer> interactWithAttachedFluidHandlers(World world, BlockPos pos,
        IFluidHandler storageFluidHandler) {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            boolean successful = TileHelpers.get(world, pos.offset(dir))
                .map(
                    te -> CapabilityHelpers.getCapability(te, CapabilityFluidHandler.FLUID_HANDLER, dir.getOpposite())
                        .map(fluidHandler -> {
                            if (isInput()) {
                                return fillFromFluidHandler(fluidHandler, storageFluidHandler, getMaxInOut(), dir);
                            } else {
                                return fillFluidHandler(fluidHandler, storageFluidHandler, getMaxInOut(), dir);
                            }
                        })
                        .orElse(false))
                .orElse(false);
            if (successful) {
                return Optional.of(FLUID_HANDLER_INTERACTION_COOLDOWN_TIME);
            }
        }

        return Optional.empty();
    }

    private int getMaxInOut() {
        return Math.max(
            FluidHelpers.BUCKET_VOLUME,
            pumpUpgradeConfig.getMaxInputOutput() * storageWrapper.getNumberOfSlotRows()
                * getAdjustedStackMultiplier(storageWrapper));
    }

    public int getAdjustedStackMultiplier(IStorageWrapper storageWrapper) {
        return 1 + (int) (pumpUpgradeConfig.getStackMultiplierRatio() * (storageWrapper.getInventoryHandler()
            .getStackSizeMultiplier() - 1));
    }

    private Optional<Integer> interactWithWorld(World world, BlockPos pos, IFluidHandler storageFluidHandler) {
        if (isInput()) {
            return fillFromBlockInRange(world, pos, storageFluidHandler);
        } else {
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                BlockPos offsetPos = pos.offset(dir);
                if (placeFluidInWorld(world, storageFluidHandler, dir, offsetPos)) {
                    return Optional.of(WORLD_INTERACTION_COOLDOWN_TIME);
                }
            }
        }
        return Optional.empty();
    }

    private boolean placeFluidInWorld(World world, IFluidHandler storageFluidHandler, ForgeDirection dir,
        BlockPos offsetPos) {
        if (dir != ForgeDirection.UP) {
            for (int tank = 0; tank < storageFluidHandler.getTanks(); tank++) {
                FluidStack tankFluid = storageFluidHandler.getFluidInTank(tank);
                if (!FluidHelpers.isEmpty(tankFluid) && fluidFilterLogic.matchesFilter(tankFluid)
                    && isValidForFluidPlacement(world, offsetPos)
                    && FluidHelpers.tryPlaceFluid(
                        null,
                        world,
                        offsetPos,
                        storageFluidHandler,
                        tankFluid,
                        ForgeDirection.UNKNOWN)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isValidForFluidPlacement(World world, BlockPos pos) {
        Block block = pos.getBlock(world);

        return pos.isAirBlock(world) || ((block instanceof BlockFluidBase || block instanceof BlockLiquid)
            && !block.isBlockSolid(world, pos.getX(), pos.getY(), pos.getZ(), ForgeDirection.UP.ordinal()));
    }

    private Optional<Integer> fillFromBlockInRange(World world, BlockPos basePos, IFluidHandler storageFluidHandler) {
        LinkedList<BlockPos> nextPositions = new LinkedList<>();
        Set<BlockPos> searchedPositions = new HashSet<>();
        nextPositions.add(basePos);
        searchedPositions.add(basePos);

        while (!nextPositions.isEmpty()) {
            BlockPos pos = nextPositions.poll();
            if (fillFromBlock(world, pos, storageFluidHandler)) {
                int distance = Math.max(1, (int) Math.ceil(Math.sqrt(basePos.distSqr(pos))));
                return Optional.of(distance * WORLD_INTERACTION_COOLDOWN_TIME);
            }

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                BlockPos offsetPos = pos.offset(dir);
                if (!searchedPositions.contains(offsetPos)) {
                    searchedPositions.add(offsetPos);
                    if (basePos.distSqr(offsetPos) <= PUMP_IN_WORLD_RANGE_SQR) {
                        nextPositions.add(offsetPos);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private boolean fillFromBlock(World world, BlockPos pos, IFluidHandler storageFluidHandler) {
        Block block = world.getBlock(pos.getX(), pos.getY(), pos.getZ());

        IFluidHandler targetFluidHandler;

        if (block instanceof IFluidBlock fluidBlock) {
            targetFluidHandler = new FluidBlockWrapper(fluidBlock, world, pos);
        } else if (block instanceof BlockLiquid liquid) {
            targetFluidHandler = new BlockLiquidWrapper(liquid, world, pos);
        } else {
            return false;
        }

        return fillFromFluidHandler(targetFluidHandler, storageFluidHandler, ForgeDirection.UP);
    }

    private boolean handleFluidContainersInHandsOfNearbyPlayers(World world, BlockPos pos,
        IFluidHandler storageFluidHandler) {
        ImmutableAxisAlignedBB searchBox = new ImmutableAxisAlignedBB(pos).grow(PLAYER_SEARCH_RANGE);
        for (EntityPlayer player : world.playerEntities) {
            if (player.boundingBox != null && searchBox.intersectsWith(player.boundingBox)
                && handleFluidContainerInHands(player, storageFluidHandler)) {
                return true;
            }
        }
        return false;
    }

    private boolean handleFluidContainerInHands(EntityPlayer player, IFluidHandler storageFluidHandler) {
        return handleFluidContainerInHand(storageFluidHandler, player);
    }

    private boolean handleFluidContainerInHand(IFluidHandler storageFluidHandler, EntityPlayer player) {
        ItemStack itemInHand = player.getCurrentEquippedItem();
        if (ItemHelpers.getCount(itemInHand) != 1 || itemInHand == storageWrapper.getWrappedStorageStack()) {
            return false;
        }
        return CapabilityHelpers.getCapability(itemInHand, CapabilityFluidHandler.FLUID_HANDLER_ITEM)
            .map(itemFluidHandler -> {
                if (isInput()) {
                    return fillFromHand(player, itemFluidHandler, storageFluidHandler, ForgeDirection.UP);
                } else {
                    return fillContainerInHand(player, itemFluidHandler, storageFluidHandler);
                }
            })
            .orElse(false);
    }

    private boolean fillContainerInHand(EntityPlayer player, IFluidHandlerItem itemFluidHandler,
        IFluidHandler storageFluidHandler) {
        boolean ret = fillFluidHandler(itemFluidHandler, storageFluidHandler, ForgeDirection.UP);
        if (ret) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, itemFluidHandler.getContainer());
        }
        return ret;
    }

    private boolean fillFluidHandler(IFluidHandler fluidHandler, IFluidHandler storageFluidHandler,
        ForgeDirection dir) {
        return fillFluidHandler(fluidHandler, storageFluidHandler, FluidHelpers.BUCKET_VOLUME, dir);
    }

    private boolean fillFluidHandler(IFluidHandler fluidHandler, IFluidHandler storageFluidHandler, int maxFill,
        ForgeDirection dir) {
        boolean ret = false;
        for (int tank = 0; tank < storageFluidHandler.getTanks(); tank++) {
            FluidStack tankFluid = storageFluidHandler.getFluidInTank(tank);
            if (!FluidHelpers.isEmpty(tankFluid) && fluidFilterLogic.matchesFilter(tankFluid)
                && !FluidHelpers.isEmpty(
                    FluidHelpers.tryFluidTransfer(
                        fluidHandler,
                        storageFluidHandler,
                        new FluidStack(tankFluid, maxFill),
                        dir,
                        true))) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    private boolean fillFromHand(EntityPlayer player, IFluidHandlerItem itemFluidHandler,
        IFluidHandler storageFluidHandler, ForgeDirection dir) {
        if (fillFromFluidHandler(itemFluidHandler, storageFluidHandler, dir)) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, itemFluidHandler.getContainer());
            return true;
        }
        return false;
    }

    private boolean fillFromFluidHandler(IFluidHandler fluidHandler, IFluidHandler storageFluidHandler,
        ForgeDirection dir) {
        return fillFromFluidHandler(fluidHandler, storageFluidHandler, FluidHelpers.BUCKET_VOLUME, dir);
    }

    private boolean fillFromFluidHandler(IFluidHandler fluidHandler, IFluidHandler storageFluidHandler, int maxDrain,
        ForgeDirection dir) {
        FluidStack containedFluid = fluidHandler.drain(maxDrain, false);
        if (!FluidHelpers.isEmpty(containedFluid) && fluidFilterLogic.matchesFilter(containedFluid)) {
            return !FluidHelpers
                .isEmpty(FluidHelpers.tryFluidTransfer(storageFluidHandler, fluidHandler, containedFluid, dir, true));
        }
        return false;
    }

    public void setIsInput(boolean input) {
        NBTHelpers.setBoolean(upgrade, "input", input);
        save();
    }

    public boolean isInput() {
        return NBTHelpers.getBoolean(upgrade, "input")
            .orElse(true);
    }

    public void setInteractWithHand(boolean interactWithHand) {
        NBTHelpers.setBoolean(upgrade, "interactWithHand", interactWithHand);
        save();
    }

    public boolean shouldInteractWithHand() {
        return NBTHelpers.getBoolean(upgrade, "interactWithHand")
            .orElse(upgradeItem.getInteractWithHandDefault());
    }

    public void setInteractWithWorld(boolean interactWithWorld) {
        NBTHelpers.setBoolean(upgrade, "interactWithWorld", interactWithWorld);
        save();
    }

    public boolean shouldInteractWithWorld() {
        return NBTHelpers.getBoolean(upgrade, "interactWithWorld")
            .orElse(upgradeItem.getInteractWithWorldDefault());
    }
}
