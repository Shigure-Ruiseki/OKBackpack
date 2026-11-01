package ruiseki.backpack.backpack.wrapper;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okcore.helper.TagHelpers;
import ruiseki.okcore.tag.TagKey;
import ruiseki.okstoragecore.api.IStorageFluidHandler;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.tank.ItemTankUpgrade;
import ruiseki.okstoragecore.upgrades.tank.TankUpgradeWrapper;

public class BackpackFluidHandler implements IStorageFluidHandler {

    private final IStorageWrapper backpackWrapper;

    public BackpackFluidHandler(IStorageWrapper backpackWrapper) {
        this.backpackWrapper = backpackWrapper;
    }

    @Override
    public int getTanks() {
        return getAllTanks().size();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return isInvalidTank(tank) ? FluidHelpers.EMPTY
            : getAllTanks().get(tank)
                .getContents();
    }

    @Nonnull
    private List<TankUpgradeWrapper> getAllTanks() {
        return backpackWrapper.getUpgradeHandler()
            .getTypeWrappers(ItemTankUpgrade.TYPE);
    }

    @Override
    public int getTankCapacity(int tank) {
        return isInvalidTank(tank) ? 0
            : getAllTanks().get(tank)
                .getTankCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
        if (isInvalidTank(tank)) {
            return false;
        }

        FluidStack contents = getAllTanks().get(tank)
            .getContents();
        return FluidHelpers.isEmpty(contents) || contents.isFluidEqual(stack);
    }

    @Override
    public int fill(FluidStack resource, boolean action, boolean ignoreInOutLimit) {
        int filled = 0;
        FluidStack toFill = resource;
        for (TankUpgradeWrapper tank : getAllTanks()) {
            filled += tank.fill(toFill, action, ignoreInOutLimit);
            if (filled == FluidHelpers.getAmount(resource)) {
                return FluidHelpers.getAmount(resource);
            }
            toFill = new FluidStack(toFill, FluidHelpers.getAmount(resource) - filled);
        }

        return filled;
    }

    @Override
    public int fill(FluidStack resource, boolean action) {
        return fill(resource, action, false);
    }

    @Override
    public FluidStack drain(TagKey<Fluid> resourceTag, int maxDrain, boolean action, boolean ignoreInOutLimit) {
        FluidStack drained = FluidHelpers.EMPTY;
        int toDrain = maxDrain;
        for (TankUpgradeWrapper tank : getAllTanks()) {
            Fluid tankFluid = tank.getContents()
                .getFluid();
            if ((FluidHelpers.isEmpty(drained) && TagHelpers.hasTag(tankFluid, resourceTag)) || tank.getContents()
                .isFluidEqual(drained)) {
                if (FluidHelpers.isEmpty(drained)) {
                    drained = tank.drain(toDrain, action, ignoreInOutLimit);
                } else {
                    FluidHelpers.grow(drained, FluidHelpers.getAmount(tank.drain(toDrain, action, ignoreInOutLimit)));
                }

                if (FluidHelpers.getAmount(drained) == maxDrain) {
                    return drained;
                }

                toDrain = maxDrain - FluidHelpers.getAmount(drained);
            }
        }

        return drained;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean action, boolean ignoreInOutLimit) {
        int drained = 0;
        int toDrain = FluidHelpers.getAmount(resource);
        for (TankUpgradeWrapper tank : getAllTanks()) {
            if (tank.getContents()
                .isFluidEqual(resource)) {
                drained += FluidHelpers.getAmount(tank.drain(toDrain, action, ignoreInOutLimit));
                if (drained == FluidHelpers.getAmount(resource)) {
                    return resource;
                }
                toDrain = FluidHelpers.getAmount(resource) - drained;
            }
        }

        return drained == 0 ? FluidHelpers.EMPTY : new FluidStack(resource, drained);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean action) {
        return drain(resource, action, false);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean action, boolean ignoreInOutLimit) {
        for (TankUpgradeWrapper tank : getAllTanks()) {
            FluidStack drained = tank.drain(maxDrain, action, ignoreInOutLimit);
            if (!FluidHelpers.isEmpty(drained)) {
                return drained;
            }
        }
        return FluidHelpers.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean action) {
        return drain(maxDrain, action, false);
    }

    private boolean isInvalidTank(int tank) {
        return tank < 0 || tank >= getTanks();
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return backpackWrapper.getWrappedStorageStack();
    }
}
