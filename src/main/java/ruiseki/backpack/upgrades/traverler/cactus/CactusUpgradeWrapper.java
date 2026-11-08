package ruiseki.backpack.upgrades.traverler.cactus;

import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class CactusUpgradeWrapper extends UpgradeWrapperBase<CactusUpgradeWrapper, ItemCactusUpgrade>
    implements ITickableUpgrade, ITravelersUpgrade {

    private static final String TICK_COUNTER_TAG = "CactusTickCounter";
    private static final int COLLECTION_INTERVAL = 600;
    private static final int WATER_AMOUNT = 1000;

    public CactusUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (entity == null) {
            if (!isRainingAt(world, pos.getX(), pos.getY() + 1, pos.getZ())) return;
        } else {
            int x = MathHelper.floor_double(entity.posX);
            int y = MathHelper.floor_double(entity.posY);
            int z = MathHelper.floor_double(entity.posZ);

            boolean inRain = isRainingAt(world, x, y + 1, z);
            boolean inWater = entity.isInWater();

            if (!inRain && !inWater) return;
        }

        tryCollectWater();
    }

    private void tryCollectWater() {
        int counter = NBTHelpers.getInteger(upgrade, TICK_COUNTER_TAG)
            .orElse(0) + 1;
        if (counter >= COLLECTION_INTERVAL) {
            counter = 0;
            FluidStack water = new FluidStack(FluidRegistry.WATER, WATER_AMOUNT);
            storageWrapper.getFluidHandler()
                .ifPresent(handler -> handler.fill(water, true));
        }
        NBTHelpers.setInteger(upgrade, TICK_COUNTER_TAG, counter);
        save();
    }

    private boolean isRainingAt(World world, int x, int y, int z) {
        if (!world.isRaining()) return false;
        if (!world.canBlockSeeTheSky(x, y, z)) return false;

        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        return biome.canSpawnLightningBolt() || biome.getEnableSnow();
    }
}
