package ruiseki.backpack.upgrades.traverler.sponge;

import java.util.function.Consumer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import org.jetbrains.annotations.Nullable;

import ruiseki.backpack.upgrades.traverler.ITravelersUpgrade;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.fluid.capability.wrapper.BlockLiquidWrapper;
import ruiseki.okcore.fluid.capability.wrapper.FluidBlockWrapper;
import ruiseki.okcore.fluid.handler.IFluidHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class SpongeUpgradeWrapper extends UpgradeWrapperBase<SpongeUpgradeWrapper, ItemSpongeUpgrade>
    implements ITravelersUpgrade, ITickableUpgrade {

    private static final int INTERVAL = 100;
    private static final int RANGE = 2;
    private static final int WATER_AMOUNT = 1000;

    public SpongeUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
    }

    @Override
    public boolean canBeDisabled() {
        return false;
    }

    @Override
    public boolean hideSettingsTab() {
        return true;
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (entity == null) return;
        tryAbsorbWater(
            world,
            MathHelper.floor_double(entity.posX),
            MathHelper.floor_double(entity.posY),
            MathHelper.floor_double(entity.posZ));
    }

    private void tryAbsorbWater(World world, int centerX, int centerY, int centerZ) {
        if (this.isInCooldown(world)) return;
        this.absorbNearbySource(world, centerX, centerY, centerZ);
        this.setCooldown(world, INTERVAL);
    }

    private void absorbNearbySource(World world, int centerX, int centerY, int centerZ) {
        this.storageWrapper.getFluidHandler()
            .ifPresent(storageFluidHandler -> {
                for (int x = centerX - RANGE; x <= centerX + RANGE; x++) {
                    for (int y = centerY - RANGE; y <= centerY + RANGE; y++) {
                        for (int z = centerZ - RANGE; z <= centerZ + RANGE; z++) {
                            if (!this.isWaterSource(world, x, y, z)) continue;

                            IFluidHandler source = this.getFluidHandler(world, x, y, z);
                            if (source == null) continue;

                            FluidStack water = source.drain(WATER_AMOUNT, false);
                            if (water == null || water.amount <= 0) continue;

                            int accepted = storageFluidHandler.fill(water, false);
                            if (accepted <= 0) continue;

                            FluidStack drained = source.drain(accepted, true);
                            if (drained == null || drained.amount <= 0) continue;

                            storageFluidHandler.fill(drained, true);
                        }
                    }
                }
            });
    }

    @Nullable
    private IFluidHandler getFluidHandler(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        BlockPos pos = new BlockPos(x, y, z);

        if (block instanceof IFluidBlock fluidBlock) {
            return new FluidBlockWrapper(fluidBlock, world, pos);
        }

        if (block instanceof BlockLiquid liquid) {
            return new BlockLiquidWrapper(liquid, world, pos);
        }

        return null;
    }

    private boolean isWaterSource(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        return (block == Blocks.water || block == Blocks.flowing_water) && world.getBlockMetadata(x, y, z) == 0;
    }
}
