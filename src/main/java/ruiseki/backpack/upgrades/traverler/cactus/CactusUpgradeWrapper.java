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
import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class CactusUpgradeWrapper extends UpgradeWrapperBase<CactusUpgradeWrapper, ItemCactusUpgrade>
    implements ITickableUpgrade, ITravelersUpgrade {

    private static final int INTERVAL = 600;

    public CactusUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (this.isInCooldown(world)) return;
        if (entity == null) {
            if (!this.isRainingAt(world, pos.getX(), pos.getY() + 1, pos.getZ())) return;
        } else {
            int x = MathHelper.floor_double(entity.posX);
            int y = MathHelper.floor_double(entity.posY);
            int z = MathHelper.floor_double(entity.posZ);

            boolean inRain = this.isRainingAt(world, x, y + 1, z);
            boolean inWater = entity.isInWater();

            if (!inRain && !inWater) return;
        }

        FluidStack water = new FluidStack(FluidRegistry.WATER, FluidHelpers.BUCKET_VOLUME);
        this.storageWrapper.getFluidHandler()
            .ifPresent(handler -> handler.fill(water, true));
        this.setCooldown(world, INTERVAL);
    }

    private boolean isRainingAt(World world, int x, int y, int z) {
        if (!world.isRaining()) return false;
        if (!world.canBlockSeeTheSky(x, y, z)) return false;

        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        return biome.canSpawnLightningBolt() || biome.getEnableSnow();
    }
}
