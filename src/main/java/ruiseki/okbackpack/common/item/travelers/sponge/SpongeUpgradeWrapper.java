package ruiseki.okbackpack.common.item.travelers.sponge;

import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITankUpgrade;
import ruiseki.okbackpack.api.wrapper.ITickable;
import ruiseki.okbackpack.api.wrapper.ITravelersUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class SpongeUpgradeWrapper extends UpgradeWrapperBase implements ITickable, ITravelersUpgrade {

    private static final String NEXT_ABSORB_TICK_TAG = "NextAbsorbTick";
    private static final int ABSORB_INTERVAL = 100;
    private static final int RANGE = 2;
    private static final int WATER_AMOUNT = 1000;

    public SpongeUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean tick(EntityPlayer player) {
        return tryAbsorbWater(
            player.worldObj,
            MathHelper.floor_double(player.posX),
            MathHelper.floor_double(player.posY),
            MathHelper.floor_double(player.posZ));
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        return tryAbsorbWater(world, pos.getX(), pos.getY(), pos.getZ());
    }

    private boolean tryAbsorbWater(World world, int centerX, int centerY, int centerZ) {
        long worldTime = world.getTotalWorldTime();
        long nextAbsorbTick = ItemNBTHelpers.getLong(upgrade, NEXT_ABSORB_TICK_TAG, 0L);
        if (nextAbsorbTick <= 0L) {
            scheduleNextTick(worldTime);
            return true;
        }
        if (worldTime < nextAbsorbTick) {
            return false;
        }

        absorbNearbySource(world, centerX, centerY, centerZ);
        scheduleNextTick(worldTime);
        return true;
    }

    private void scheduleNextTick(long worldTime) {
        ItemNBTHelpers.setLong(upgrade, NEXT_ABSORB_TICK_TAG, worldTime + ABSORB_INTERVAL);
        save();
    }

    private boolean absorbNearbySource(World world, int centerX, int centerY, int centerZ) {
        Map<Integer, ITankUpgrade> tanks = storage.gatherCapabilityUpgrades(ITankUpgrade.class);

        boolean absorbedAny = false;

        for (int x = centerX - RANGE; x <= centerX + RANGE; x++) {
            for (int y = centerY - RANGE; y <= centerY + RANGE; y++) {
                for (int z = centerZ - RANGE; z <= centerZ + RANGE; z++) {

                    if (!isWaterSource(world, x, y, z)) continue;

                    world.setBlock(x, y, z, Blocks.air, 0, 3);
                    absorbedAny = true;

                    FluidStack water = new FluidStack(FluidRegistry.WATER, WATER_AMOUNT);

                    for (ITankUpgrade tank : tanks.values()) {
                        tank.fill(water, true);
                    }
                }
            }
        }

        return absorbedAny;
    }

    private boolean isWaterSource(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block == null || block.getMaterial() != Material.water) return false;
        if (block != Blocks.water && block != Blocks.flowing_water) return false;
        return world.getBlockMetadata(x, y, z) == 0;
    }
}
