package ruiseki.okstoragecore.upgrades;

import java.util.function.Predicate;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okcore.fluid.handler.FluidTank;
import ruiseki.okcore.fluid.handler.IFluidHandler;
import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okcore.persist.nbt.INBTSerializable;

public class FluidTankHandler implements IFluidHandler, INBTSerializable {

    protected final FluidTank[] tanks;

    private boolean onlyEmptyFilters = true;

    public FluidTankHandler(int size) {
        this.tanks = new FluidTank[size];

        for (int i = 0; i < size; i++) {
            int slot = i;

            this.tanks[i] = new FluidTank(FluidHelpers.BUCKET_VOLUME) {

                @Override
                protected void onContentsChanged() {
                    super.onContentsChanged();
                    FluidTankHandler.this.onContentsChanged(slot);
                }

                @Override
                public boolean isFluidValid(int tank, FluidStack stack) {
                    return FluidTankHandler.this.isFluidValid(slot, stack);
                }
            };
        }
    }

    protected void onContentsChanged(int slot) {
        this.updateEmptyFilters();
    }

    public FluidTank getTank(int slot) {
        return this.tanks[slot];
    }

    public void setFluid(int slot, FluidStack fluid) {
        this.getTank(slot)
            .setFluid(FluidHelpers.isEmpty(fluid) ? FluidHelpers.EMPTY : FluidHelpers.copy(fluid));
    }

    @Override
    public int getTanks() {
        return this.tanks.length;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return this.tanks[tank].getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.tanks[tank].getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, boolean action) {
        if (FluidHelpers.isEmpty(resource)) return 0;

        int remaining = FluidHelpers.getAmount(resource);
        int filled = 0;

        for (FluidTank tank : this.tanks) {
            if (remaining <= 0) break;

            FluidStack toFill = FluidHelpers.copy(resource);
            FluidHelpers.setAmount(toFill, remaining);

            int amount = tank.fill(toFill, action);

            filled += amount;
            remaining -= amount;
        }

        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean action) {
        if (FluidHelpers.isEmpty(resource)) return FluidHelpers.EMPTY;

        int remaining = FluidHelpers.getAmount(resource);
        FluidStack drained = FluidHelpers.EMPTY;
        for (FluidTank tank : this.tanks) {
            if (remaining <= 0) break;

            FluidStack current = tank.getFluid();

            if (FluidHelpers.isEmpty(current) || !current.isFluidEqual(resource)) {
                continue;
            }

            FluidStack result = tank.drain(remaining, action);

            if (FluidHelpers.isEmpty(result)) {
                continue;
            }

            if (drained == null) {
                drained = FluidHelpers.copy(result);
            } else {
                FluidHelpers.grow(drained, FluidHelpers.getAmount(result));
            }

            remaining -= FluidHelpers.getAmount(result);
        }

        return FluidHelpers.isEmpty(drained) ? FluidHelpers.EMPTY : drained;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean action) {
        if (maxDrain <= 0) return FluidHelpers.EMPTY;
        for (FluidTank tank : this.tanks) {
            FluidStack result = tank.drain(maxDrain, action);
            if (!FluidHelpers.isEmpty(result)) {
                return result;
            }
        }
        return FluidHelpers.EMPTY;
    }

    public int fillTank(int tank, FluidStack resource, boolean action) {
        return this.tanks[tank].fill(resource, action);
    }

    public FluidStack drainTank(int tank, int maxDrain, boolean action) {
        return this.tanks[tank].drain(maxDrain, action);
    }

    protected void onLoad() {
        this.updateEmptyFilters();
    }

    private void updateEmptyFilters() {
        this.onlyEmptyFilters = true;

        for (FluidTank tank : this.tanks) {
            if (!FluidHelpers.isEmpty(tank.getFluid())) {
                this.onlyEmptyFilters = false;
                return;
            }
        }
    }

    public boolean hasOnlyEmptyFilters() {
        return this.onlyEmptyFilters;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();

        for (FluidTank tank : this.tanks) {
            tank.writeToNBT(tag);
        }

        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        for (FluidTank tank : this.tanks) {
            tank.readFromNBT(tag);
        }
        this.onLoad();
    }

    public boolean doesNotContain(Predicate<FluidStack> matches) {
        for (FluidTank tank : tanks) {
            FluidStack fluid = tank.getFluid();

            if (fluid != null && matches.test(fluid)) {
                return false;
            }
        }

        return true;
    }
}
