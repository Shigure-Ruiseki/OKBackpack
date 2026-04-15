package ruiseki.okbackpack.common.item.cactus;

import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ITankUpgrade;
import ruiseki.okbackpack.api.wrapper.ITickable;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class CactusUpgradeWrapper extends UpgradeWrapperBase implements ITickable {

    private static final String TICK_COUNTER_TAG = "CactusTickCounter";
    private static final int COLLECTION_INTERVAL = 600;
    private static final int WATER_AMOUNT = 1000;

    public CactusUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean tick(EntityPlayer player) {
        World world = player.worldObj;
        int x = MathHelper.floor_double(player.posX);
        int y = MathHelper.floor_double(player.posY);
        int z = MathHelper.floor_double(player.posZ);

        boolean inRain = isRainingAt(world, x, y + 1, z);
        boolean inWater = player.isInWater();

        if (!inRain && !inWater) return false;

        return tryCollectWater(world);
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        if (!isRainingAt(world, pos.getX(), pos.getY() + 1, pos.getZ())) return false;

        return tryCollectWater(world);
    }

    private boolean tryCollectWater(World world) {
        int counter = ItemNBTHelpers.getInt(upgrade, TICK_COUNTER_TAG, 0);
        counter++;

        if (counter >= COLLECTION_INTERVAL) {
            counter = 0;

            Map<Integer, ITankUpgrade> tanks = storage.gatherCapabilityUpgrades(ITankUpgrade.class);
            if (!tanks.isEmpty()) {
                FluidStack water = new FluidStack(FluidRegistry.WATER, WATER_AMOUNT);
                for (ITankUpgrade tank : tanks.values()) {
                    int filled = tank.fill(water, true);
                    if (filled > 0) {
                        ItemNBTHelpers.setInt(upgrade, TICK_COUNTER_TAG, counter);
                        save();
                        return true;
                    }
                }
            }
        }

        ItemNBTHelpers.setInt(upgrade, TICK_COUNTER_TAG, counter);
        save();
        return true;
    }

    private boolean isRainingAt(World world, int x, int y, int z) {
        if (!world.isRaining()) return false;
        if (!world.canBlockSeeTheSky(x, y, z)) return false;
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        return biome.canSpawnLightningBolt() || biome.getEnableSnow();
    }
}
