package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraftforge.fluids.FluidStack;

public class DelegatedFluidStack implements Supplier<FluidStack>, Consumer<FluidStack> {

    private Supplier<FluidStack> getter;
    private Consumer<FluidStack> setter;

    public DelegatedFluidStack(Supplier<FluidStack> getter, Consumer<FluidStack> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public void setDelegated(Supplier<FluidStack> getter, Consumer<FluidStack> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public FluidStack get() {
        return getter.get();
    }

    @Override
    public void accept(FluidStack value) {
        setter.accept(value);
    }
}
