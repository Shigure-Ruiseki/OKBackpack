
package ruiseki.okstoragecore.api;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import ruiseki.okcore.config.configurabletypeaction.FluidAction;
import ruiseki.okcore.fluid.handler.IFluidHandlerItem;
import ruiseki.okcore.fluid.handler.IFluidTankProperties;
import ruiseki.okcore.tag.TagKey;

public interface IStorageFluidHandler extends IFluidHandlerItem {

    default int fill(TagKey<Fluid> fluidTag, int maxFill, Fluid fallbackFluid, FluidAction action) {
        return fill(fluidTag, maxFill, fallbackFluid, action, false);
    }

    default int fill(TagKey<Fluid> fluidTag, int maxFill, Fluid fallbackFluid, FluidAction action,
        boolean ignoreInOutLimit) {
        if (maxFill <= 0) {
            return 0;
        }

        IFluidTankProperties[] properties = getTankProperties();

        if (properties != null) {
            for (IFluidTankProperties property : properties) {
                if (property == null) {
                    continue;
                }

                FluidStack tankFluid = property.getContents();

                if (tankFluid == null || tankFluid.getFluid() == null) {
                    continue;
                }

                if (matchesFluidTag(tankFluid.getFluid(), fluidTag)) {
                    FluidStack resource = tankFluid.copy();
                    resource.amount = maxFill;

                    return fill(resource, action, ignoreInOutLimit);
                }
            }
        }

        if (fallbackFluid == null) {
            return 0;
        }

        return fill(new FluidStack(fallbackFluid, maxFill), action, ignoreInOutLimit);
    }

    /**
     * Override this method to use your TagKey registry.
     */
    default boolean matchesFluidTag(Fluid fluid, TagKey<Fluid> tag) {
        return false;
    }

    int fill(FluidStack resource, FluidAction action, boolean ignoreInOutLimit);

    FluidStack drain(TagKey<Fluid> resourceTag, int maxDrain, FluidAction action, boolean ignoreInOutLimit);

    FluidStack drain(FluidStack resource, FluidAction action, boolean ignoreInOutLimit);

    FluidStack drain(int maxDrain, FluidAction action, boolean ignoreInOutLimit);
}
