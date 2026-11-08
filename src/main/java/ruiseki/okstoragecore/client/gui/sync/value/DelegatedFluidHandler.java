
package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import org.jetbrains.annotations.NotNull;

import ruiseki.okcore.fluid.handler.IFluidHandler;
import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okstoragecore.upgrades.FluidTankHandler;

public class DelegatedFluidHandler implements IFluidHandler {

    private Supplier<FluidTankHandler> delegated;
    private final int wrappedTankAmount;
    private final List<IFluidTank> tanks;

    public DelegatedFluidHandler(Supplier<FluidTankHandler> delegated, int wrappedTankAmount) {
        if (wrappedTankAmount < 0) {
            throw new IllegalArgumentException("wrappedTankAmount cannot be negative");
        }

        this.delegated = delegated;
        this.wrappedTankAmount = wrappedTankAmount;

        List<IFluidTank> tanks = new ArrayList<>(wrappedTankAmount);

        for (int i = 0; i < wrappedTankAmount; i++) {
            tanks.add(new DelegatedFluidTank(i, this));
        }

        this.tanks = Collections.unmodifiableList(tanks);
    }

    public void setDelegated(Supplier<FluidTankHandler> delegated) {
        this.delegated = delegated;
    }

    public FluidTankHandler get() {
        return this.delegated.get();
    }

    public IFluidTank getTank(int tank) {
        return this.tanks.get(tank);
    }

    @Override
    public int getTanks() {
        FluidTankHandler handler = this.get();

        if (handler.getTanks() != this.wrappedTankAmount) {
            throw new IllegalStateException(
                "Mismatched delegated fluid handler tank amount: expected " + this.wrappedTankAmount
                    + " but got "
                    + handler.getTanks());
        }

        return this.wrappedTankAmount;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return this.get()
            .getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return this.get()
            .getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return this.get()
            .isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, boolean action) {
        return this.get()
            .fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean action) {
        return this.get()
            .drain(resource, action);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean action) {
        return this.get()
            .drain(maxDrain, action);
    }

    public int fillTank(int tank, FluidStack resource, boolean action) {
        return this.get()
            .fillTank(tank, resource, action);
    }

    public FluidStack drainTank(int tank, int maxDrain, boolean action) {
        return this.get()
            .drainTank(tank, maxDrain, action);
    }

    public static class DelegatedFluidTank implements IFluidTank {

        private final int tank;
        private final DelegatedFluidHandler handler;

        public DelegatedFluidTank(int tank, DelegatedFluidHandler handler) {
            this.tank = tank;
            this.handler = handler;
        }

        @Override
        public FluidStack getFluid() {
            return this.handler.getFluidInTank(this.tank);
        }

        @Override
        public int getFluidAmount() {
            return FluidHelpers.getAmount(this.getFluid());
        }

        @Override
        public int getCapacity() {
            return this.handler.getTankCapacity(this.tank);
        }

        @Override
        public FluidTankInfo getInfo() {
            return new FluidTankInfo(this);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return this.handler.fillTank(this.tank, resource, doFill);
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return this.handler.drainTank(this.tank, maxDrain, doDrain);
        }
    }
}
