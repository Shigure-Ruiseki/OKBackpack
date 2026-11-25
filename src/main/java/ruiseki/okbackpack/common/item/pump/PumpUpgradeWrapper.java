package ruiseki.okbackpack.common.item.pump;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IPumpUpgrade;
import ruiseki.okbackpack.api.wrapper.ITankUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class PumpUpgradeWrapper extends UpgradeWrapperBase implements IPumpUpgrade {

    public static final String IS_INPUT_TAG = "PumpIsInput";
    public static final String INTERACT_HAND_TAG = "PumpInteractHand";
    public static final String INTERACT_WORLD_TAG = "PumpInteractWorld";
    public static final String INTERACT_HANDLERS_TAG = "PumpInteractHandlers";

    protected static final int HAND_COOLDOWN_TICKS = 3;
    protected static final int HANDLERS_COOLDOWN_TICKS = 5;
    protected static final int WORLD_COOLDOWN_TICKS = 20;
    protected static final int IDLE_COOLDOWN_TICKS = 40;

    protected static final int BASE_MAX_TRANSFER = 1000;
    protected static final int PLAYER_SEARCH_RANGE = 3;
    protected static final int WORLD_BFS_RANGE = 4;
    protected static final int WORLD_BFS_RANGE_SQR = WORLD_BFS_RANGE * WORLD_BFS_RANGE;

    private final boolean defaultInteractWithHand;
    private final boolean defaultInteractWithWorld;
    private final boolean defaultInteractWithFluidHandlers;

    private int cooldown = 0;

    public PumpUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer,
        boolean defaultInteractWithHand, boolean defaultInteractWithWorld, boolean defaultInteractWithFluidHandlers) {
        super(upgrade, storage, upgradeConsumer);
        this.defaultInteractWithHand = defaultInteractWithHand;
        this.defaultInteractWithWorld = defaultInteractWithWorld;
        this.defaultInteractWithFluidHandlers = defaultInteractWithFluidHandlers;
    }

    @Override
    public boolean isInput() {
        return ItemNBTHelpers.getBoolean(upgrade, IS_INPUT_TAG, true);
    }

    @Override
    public void setInput(boolean input) {
        ItemNBTHelpers.setBoolean(upgrade, IS_INPUT_TAG, input);
        save();
    }

    @Override
    public boolean shouldInteractWithHand() {
        return ItemNBTHelpers.getBoolean(upgrade, INTERACT_HAND_TAG, defaultInteractWithHand);
    }

    @Override
    public void setInteractWithHand(boolean interact) {
        ItemNBTHelpers.setBoolean(upgrade, INTERACT_HAND_TAG, interact);
        save();
    }

    @Override
    public boolean shouldInteractWithWorld() {
        return ItemNBTHelpers.getBoolean(upgrade, INTERACT_WORLD_TAG, defaultInteractWithWorld);
    }

    @Override
    public void setInteractWithWorld(boolean interact) {
        ItemNBTHelpers.setBoolean(upgrade, INTERACT_WORLD_TAG, interact);
        save();
    }

    @Override
    public boolean shouldInteractWithFluidHandlers() {
        return ItemNBTHelpers.getBoolean(upgrade, INTERACT_HANDLERS_TAG, defaultInteractWithFluidHandlers);
    }

    @Override
    public void setInteractWithFluidHandlers(boolean interact) {
        ItemNBTHelpers.setBoolean(upgrade, INTERACT_HANDLERS_TAG, interact);
        save();
    }

    @Override
    public boolean isAdvanced() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return ItemNBTHelpers.getBoolean(upgrade, ENABLED_TAG, true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        ItemNBTHelpers.setBoolean(upgrade, ENABLED_TAG, enabled);
        save();
    }

    @Override
    public void toggle() {
        setEnabled(!isEnabled());
    }

    @Override
    public boolean hasTankAvailable() {
        return findTank() != null;
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.pump_settings";
    }

    /**
     * Locates the first tank upgrade installed in the same backpack, or {@code null} if none.
     */
    protected ITankUpgrade findTank() {
        Map<Integer, ITankUpgrade> tanks = storage.gatherCapabilityUpgrades(ITankUpgrade.class);
        if (tanks.isEmpty()) return null;
        return tanks.values()
            .iterator()
            .next();
    }

    protected int getMaxInOut() {
        double multiplier = storage.applyStackLimitModifiers();
        return Math.max(BASE_MAX_TRANSFER, (int) (BASE_MAX_TRANSFER * multiplier));
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (player == null || player.worldObj.isRemote) return false;
        if (!isEnabled()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        ITankUpgrade tank = findTank();
        if (tank == null) {
            cooldown = IDLE_COOLDOWN_TICKS;
            return false;
        }
        // Basic pump only acts via the surrounding-blocks tick path; advanced overrides this method.
        if (!isAdvanced()) {
            cooldown = IDLE_COOLDOWN_TICKS;
            return false;
        }
        if (shouldInteractWithHand() && tryHandInteraction(tank, player)) {
            cooldown = HAND_COOLDOWN_TICKS;
            return true;
        }
        // Advanced pump worn on the player can also act on the world / nearby fluid handlers,
        // using the player's own block position. This lets it e.g. place a water block on the
        // player's feet.
        BlockPos playerPos = new BlockPos(
            net.minecraft.util.MathHelper.floor_double(player.posX),
            net.minecraft.util.MathHelper.floor_double(player.posY),
            net.minecraft.util.MathHelper.floor_double(player.posZ));
        Optional<Integer> result = handleInWorldInteractions(tank, player.worldObj, playerPos);
        if (result.isPresent()) {
            cooldown = result.get();
            return true;
        }
        cooldown = IDLE_COOLDOWN_TICKS;
        return false;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        if (world == null || world.isRemote) return false;
        if (!isEnabled()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        ITankUpgrade tank = findTank();
        if (tank == null) {
            cooldown = IDLE_COOLDOWN_TICKS;
            return false;
        }

        // Basic pump only interacts with adjacent fluid handlers (no hand, no world).
        if (!isAdvanced()) {
            if (tryAdjacentFluidHandlers(tank, world, pos)) {
                cooldown = HANDLERS_COOLDOWN_TICKS;
                return true;
            }
            cooldown = IDLE_COOLDOWN_TICKS;
            return false;
        }

        if (shouldInteractWithHand() && tryNearbyPlayerHand(tank, world, pos)) {
            cooldown = HAND_COOLDOWN_TICKS;
            return true;
        }

        Optional<Integer> result = handleInWorldInteractions(tank, world, pos);
        if (result.isPresent()) {
            cooldown = result.get();
            return true;
        }

        cooldown = IDLE_COOLDOWN_TICKS;
        return false;
    }

    private Optional<Integer> handleInWorldInteractions(ITankUpgrade tank, World world, BlockPos pos) {
        if (shouldInteractWithFluidHandlers()) {
            if (tryAdjacentFluidHandlers(tank, world, pos)) {
                return Optional.of(HANDLERS_COOLDOWN_TICKS);
            }
        }
        if (shouldInteractWithWorld()) {
            if (isInput()) {
                Optional<Integer> drained = bfsDrainFromWorld(tank, world, pos);
                if (drained.isPresent()) return drained;
            } else {
                if (placeFluidInAdjacentAir(tank, world, pos)) {
                    return Optional.of(WORLD_COOLDOWN_TICKS);
                }
            }
        }
        return Optional.empty();
    }

    private boolean tryAdjacentFluidHandlers(ITankUpgrade tank, World world, BlockPos pos) {
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int nx = pos.x + dir.offsetX;
            int ny = pos.y + dir.offsetY;
            int nz = pos.z + dir.offsetZ;
            TileEntity te = world.getTileEntity(nx, ny, nz);
            if (!(te instanceof IFluidHandler handler)) continue;
            ForgeDirection from = dir.getOpposite();
            if (transferWithHandler(tank, handler, from, getMaxInOut())) {
                return true;
            }
        }
        return false;
    }

    private boolean transferWithHandler(ITankUpgrade tank, IFluidHandler handler, ForgeDirection from, int maxAmount) {
        if (isInput()) {
            FluidStack drained = handler.drain(from, maxAmount, false);
            if (drained == null || drained.amount <= 0) return false;
            if (!passesFluidFilter(drained)) return false;
            int filled = tank.fill(drained, false);
            if (filled <= 0) return false;
            FluidStack actual = handler.drain(from, filled, true);
            if (actual == null || actual.amount <= 0) return false;
            tank.fill(actual, true);
            return true;
        }
        FluidStack inTank = tank.getContents();
        if (inTank == null || inTank.amount <= 0) return false;
        if (!passesFluidFilter(inTank)) return false;
        FluidStack toFill = new FluidStack(inTank, Math.min(maxAmount, inTank.amount));
        int filled = handler.fill(from, toFill, false);
        if (filled <= 0) return false;
        FluidStack drained = tank.drain(filled, true);
        if (drained == null || drained.amount <= 0) return false;
        handler.fill(from, drained, true);
        return true;
    }

    private boolean tryHandInteraction(ITankUpgrade tank, EntityPlayer player) {
        return handleHand(tank, player, 0) || handleHand(tank, player, 1);
    }

    private boolean tryNearbyPlayerHand(ITankUpgrade tank, World world, BlockPos pos) {
        double cx = pos.x + 0.5;
        double cy = pos.y + 0.5;
        double cz = pos.z + 0.5;
        for (Object obj : world.playerEntities) {
            if (!(obj instanceof EntityPlayer player)) continue;
            double dx = player.posX - cx;
            double dy = player.posY - cy;
            double dz = player.posZ - cz;
            if (dx * dx + dy * dy + dz * dz > PLAYER_SEARCH_RANGE * PLAYER_SEARCH_RANGE) continue;
            if (tryHandInteraction(tank, player)) return true;
        }
        return false;
    }

    /**
     * Handles the player's main-hand (slot 0) or off-hand-equivalent (slot 1, currently main hand again
     * because vanilla 1.7.10 has no off-hand). Defensive code remains symmetrical with SC behavior.
     */
    private boolean handleHand(ITankUpgrade tank, EntityPlayer player, int handIdx) {
        ItemStack inHand = handIdx == 0 ? player.getHeldItem() : null;
        if (inHand == null || inHand.stackSize <= 0) return false;
        if (inHand == upgrade) return false;

        if (isInput()) {
            return drainFromHandStack(tank, player, inHand);
        }
        return fillIntoHandStack(tank, player, inHand);
    }

    private boolean drainFromHandStack(ITankUpgrade tank, EntityPlayer player, ItemStack handStack) {
        if (handStack.getItem() instanceof IFluidContainerItem container) {
            FluidStack contained = container.getFluid(handStack);
            if (contained == null || contained.amount <= 0) return false;
            if (!passesFluidFilter(contained)) return false;
            int simFill = tank.fill(contained, false);
            if (simFill <= 0) return false;
            FluidStack actuallyDrained = container.drain(handStack, simFill, true);
            if (actuallyDrained == null || actuallyDrained.amount <= 0) return false;
            tank.fill(actuallyDrained, true);
            return true;
        }

        FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(handStack);
        if (contained == null || contained.amount <= 0) return false;
        if (!passesFluidFilter(contained)) return false;
        int simFill = tank.fill(contained, false);
        if (simFill < contained.amount) return false; // need to drain whole container
        ItemStack emptied = FluidContainerRegistry.drainFluidContainer(handStack);
        if (emptied == null) return false;
        tank.fill(contained, true);
        player.setCurrentItemOrArmor(0, emptied);
        return true;
    }

    private boolean fillIntoHandStack(ITankUpgrade tank, EntityPlayer player, ItemStack handStack) {
        FluidStack fluid = tank.getContents();
        if (fluid == null || fluid.amount <= 0) return false;
        if (!passesFluidFilter(fluid)) return false;

        if (handStack.getItem() instanceof IFluidContainerItem container) {
            FluidStack toFill = new FluidStack(fluid, Math.min(getMaxInOut(), fluid.amount));
            int filled = container.fill(handStack, toFill, true);
            if (filled <= 0) return false;
            tank.drain(filled, true);
            return true;
        }

        if (FluidContainerRegistry.isEmptyContainer(handStack)) {
            ItemStack filled = FluidContainerRegistry.fillFluidContainer(fluid, handStack);
            if (filled == null) return false;
            FluidStack used = FluidContainerRegistry.getFluidForFilledItem(filled);
            if (used == null || used.amount <= 0) return false;
            FluidStack drained = tank.drain(used.amount, false);
            if (drained == null || drained.amount < used.amount) return false;
            tank.drain(used.amount, true);
            player.setCurrentItemOrArmor(0, filled);
            return true;
        }
        return false;
    }

    private Optional<Integer> bfsDrainFromWorld(ITankUpgrade tank, World world, BlockPos basePos) {
        LinkedList<long[]> queue = new LinkedList<>();
        Set<Long> visited = new HashSet<>();
        long baseKey = encode(basePos.x, basePos.y, basePos.z);
        queue.add(new long[] { basePos.x, basePos.y, basePos.z });
        visited.add(baseKey);

        while (!queue.isEmpty()) {
            long[] cur = queue.poll();
            int cx = (int) cur[0];
            int cy = (int) cur[1];
            int cz = (int) cur[2];

            if (drainFluidBlockAt(tank, world, cx, cy, cz)) {
                int dx = cx - basePos.x;
                int dy = cy - basePos.y;
                int dz = cz - basePos.z;
                int dist = Math.max(1, (int) Math.sqrt(dx * dx + dy * dy + dz * dz));
                return Optional.of(dist * WORLD_COOLDOWN_TICKS);
            }

            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                int nx = cx + dir.offsetX;
                int ny = cy + dir.offsetY;
                int nz = cz + dir.offsetZ;
                long key = encode(nx, ny, nz);
                if (!visited.add(key)) continue;
                int rx = nx - basePos.x;
                int ry = ny - basePos.y;
                int rz = nz - basePos.z;
                if (rx * rx + ry * ry + rz * rz < WORLD_BFS_RANGE_SQR) {
                    queue.add(new long[] { nx, ny, nz });
                }
            }
        }
        return Optional.empty();
    }

    private boolean drainFluidBlockAt(ITankUpgrade tank, World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block == null || block == Blocks.air) return false;

        if (block instanceof IFluidBlock fluidBlock) {
            if (!fluidBlock.canDrain(world, x, y, z)) return false;
            FluidStack simulated = fluidBlock.drain(world, x, y, z, false);
            if (simulated == null || simulated.amount <= 0) return false;
            if (!passesFluidFilter(simulated)) return false;
            int filled = tank.fill(simulated, false);
            if (filled < simulated.amount) return false;
            FluidStack drained = fluidBlock.drain(world, x, y, z, true);
            if (drained == null || drained.amount <= 0) return false;
            tank.fill(drained, true);
            return true;
        }

        Material mat = block.getMaterial();
        Fluid fluid;
        if (mat == Material.water && world.getBlockMetadata(x, y, z) == 0) {
            fluid = FluidRegistry.WATER;
        } else if (mat == Material.lava && world.getBlockMetadata(x, y, z) == 0) {
            fluid = FluidRegistry.LAVA;
        } else {
            return false;
        }
        FluidStack stack = new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME);
        if (!passesFluidFilter(stack)) return false;
        int filled = tank.fill(stack, false);
        if (filled < stack.amount) return false;
        world.setBlockToAir(x, y, z);
        tank.fill(stack, true);
        return true;
    }

    private boolean placeFluidInAdjacentAir(ITankUpgrade tank, World world, BlockPos pos) {
        FluidStack contents = tank.getContents();
        if (contents == null || contents.amount < FluidContainerRegistry.BUCKET_VOLUME) return false;
        if (!passesFluidFilter(contents)) return false;
        Fluid fluid = contents.getFluid();
        if (fluid == null || fluid.getBlock() == null) return false;

        // Try the position itself first (so that when this is called with the player's own block
        // pos, the fluid can be placed where the player is standing).
        if (tryPlaceFluidAt(tank, world, fluid, pos.x, pos.y, pos.z)) return true;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (dir == ForgeDirection.UP) continue;
            int nx = pos.x + dir.offsetX;
            int ny = pos.y + dir.offsetY;
            int nz = pos.z + dir.offsetZ;
            if (tryPlaceFluidAt(tank, world, fluid, nx, ny, nz)) return true;
        }
        return false;
    }

    private boolean tryPlaceFluidAt(ITankUpgrade tank, World world, Fluid fluid, int x, int y, int z) {
        Block existing = world.getBlock(x, y, z);
        boolean replaceable = world.isAirBlock(x, y, z) || (existing != null && !existing.getMaterial()
            .isSolid()
            && existing.getMaterial() != Material.water
            && existing.getMaterial() != Material.lava
            && !(existing instanceof IFluidBlock));
        if (!replaceable) return false;
        FluidStack drained = tank.drain(FluidContainerRegistry.BUCKET_VOLUME, false);
        if (drained == null || drained.amount < FluidContainerRegistry.BUCKET_VOLUME) return false;
        world.setBlock(x, y, z, fluid.getBlock(), 0, 3);
        tank.drain(FluidContainerRegistry.BUCKET_VOLUME, true);
        return true;
    }

    private static long encode(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (z & 0x3FFFFFF) << 12) | (y & 0xFFF);
    }
}
