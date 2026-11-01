package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.Supplier;

import net.minecraftforge.fluids.FluidStack;

public class DelegatedFluidStack implements Supplier<FluidStack> {

    private Supplier<FluidStack> delegated;

    public DelegatedFluidStack(Supplier<FluidStack> delegated) {
        this.delegated = delegated;
    }

    public void setDelegated(Supplier<FluidStack> delegated) {
        this.delegated = delegated;
    }

    @Override
    public FluidStack get() {
        return delegated.get();
    }
}
