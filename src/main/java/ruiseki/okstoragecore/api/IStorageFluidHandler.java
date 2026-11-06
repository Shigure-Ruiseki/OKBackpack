
package ruiseki.okstoragecore.api;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import ruiseki.okcore.fluid.handler.IFluidHandlerItem;
import ruiseki.okcore.helper.TagHelpers;
import ruiseki.okcore.tag.TagKey;

public interface IStorageFluidHandler extends IFluidHandlerItem {

    default int fill(TagKey<Fluid> fluidTag, int maxFill, Fluid fallbackFluid, boolean action) {
        return this.fill(fluidTag, maxFill, fallbackFluid, action, false);
    }

    default int fill(TagKey<Fluid> fluidTag, int maxFill, Fluid fallbackFluid, boolean action,
        boolean ignoreInOutLimit) {
        for (int tank = 0; tank < this.getTanks(); tank++) {
            FluidStack tankFluid = this.getFluidInTank(tank);
            if (TagHelpers.hasTag(tankFluid, fluidTag)) {
                return this.fill(new FluidStack(tankFluid.getFluid(), maxFill), action, ignoreInOutLimit);
            }
        }
        return this.fill(new FluidStack(fallbackFluid, maxFill), action, ignoreInOutLimit);
    }

    int fill(FluidStack resource, boolean action, boolean ignoreInOutLimit);

    FluidStack drain(TagKey<Fluid> resourceTag, int maxDrain, boolean action, boolean ignoreInOutLimit);

    FluidStack drain(FluidStack resource, boolean action, boolean ignoreInOutLimit);

    FluidStack drain(int maxDrain, boolean action, boolean ignoreInOutLimit);
}
