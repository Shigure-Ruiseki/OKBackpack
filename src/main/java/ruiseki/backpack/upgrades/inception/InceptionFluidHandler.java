package ruiseki.backpack.upgrades.inception;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.fluid.capability.EmptyFluidHandler;
import ruiseki.okcore.fluid.handler.IFluidHandler;
import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okcore.tag.TagKey;
import ruiseki.okstoragecore.api.IStorageFluidHandler;

public class InceptionFluidHandler implements IStorageFluidHandler {

    @Nullable
    private final IStorageFluidHandler wrappedFluidHandler;
    private final InventoryOrder inventoryOrder;
    private final SubBackpacksHandler subBackpacksHandler;
    private IStorageFluidHandler[] fluidHandlers;
    protected int[] baseIndex;
    protected int tankCount;
    private final ItemStack backpack;

    public InceptionFluidHandler(@Nullable IStorageFluidHandler wrappedFluidHandler, ItemStack backpack,
        InventoryOrder inventoryOrder, SubBackpacksHandler subBackpacksHandler) {
        this.wrappedFluidHandler = wrappedFluidHandler;
        this.backpack = backpack;
        this.inventoryOrder = inventoryOrder;
        this.subBackpacksHandler = subBackpacksHandler;
        subBackpacksHandler.addRefreshListener(sbs -> refreshHandlers());
        refreshHandlers();
    }

    private void refreshHandlers() {
        List<IStorageFluidHandler> handlers = new ArrayList<>();
        if (wrappedFluidHandler != null && inventoryOrder == InventoryOrder.MAIN_FIRST) {
            handlers.add(wrappedFluidHandler);
        }
        subBackpacksHandler.getSubBackpacks()
            .forEach(
                sbp -> sbp.getFluidHandler()
                    .ifPresent(handlers::add));
        if (wrappedFluidHandler != null && inventoryOrder == InventoryOrder.INCEPTED_FIRST) {
            handlers.add(wrappedFluidHandler);
        }
        fluidHandlers = handlers.toArray(new IStorageFluidHandler[] {});
        baseIndex = new int[fluidHandlers.length];
        int index = 0;
        for (int i = 0; i < fluidHandlers.length; i++) {
            index += fluidHandlers[i].getTanks();
            baseIndex[i] = index;
        }
        tankCount = index;
    }

    @Override
    public int getTanks() {
        return tankCount;
    }

    private int getHandlerIndexForTank(int slot) {
        if (slot < 0) {
            return -1;
        }

        for (int i = 0; i < baseIndex.length; i++) {
            if (slot - baseIndex[i] < 0) {
                return i;
            }
        }
        return -1;
    }

    protected IFluidHandler getHandlerFromIndex(int index) {
        if (index < 0 || index >= fluidHandlers.length) {
            return EmptyFluidHandler.INSTANCE;
        }
        return fluidHandlers[index];
    }

    protected int getTankFromIndex(int tank, int handlerIndex) {
        if (handlerIndex <= 0 || handlerIndex >= baseIndex.length) {
            return tank;
        }
        return tank - baseIndex[handlerIndex - 1];
    }

    private <T> T getFluidHandlerValue(int overallTank, BiFunction<IFluidHandler, Integer, T> getValue) {
        int handlerIndex = getHandlerIndexForTank(overallTank);
        return getValue.apply(getHandlerFromIndex(handlerIndex), getTankFromIndex(overallTank, handlerIndex));
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return getFluidHandlerValue(tank, IFluidHandler::getFluidInTank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return getFluidHandlerValue(tank, IFluidHandler::getTankCapacity);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return getFluidHandlerValue(tank, (h, t) -> h.isFluidValid(t, stack));
    }

    @Override
    public int fill(FluidStack resource, boolean action, boolean ignoreInOutLimit) {
        int filled = 0;
        FluidStack toFill = resource;
        for (IStorageFluidHandler fluidHandler : fluidHandlers) {
            filled += fluidHandler.fill(toFill, action, ignoreInOutLimit);
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
        FluidStack drainedStack = FluidHelpers.EMPTY;
        FluidStack stackToDrain = FluidHelpers.EMPTY;
        for (IStorageFluidHandler fluidHandler : fluidHandlers) {
            if (FluidHelpers.isEmpty(drainedStack)) {
                drainedStack = fluidHandler.drain(resourceTag, maxDrain, action, ignoreInOutLimit);
                if (FluidHelpers.getAmount(drainedStack) == maxDrain) {
                    return drainedStack;
                }
                if (!FluidHelpers.isEmpty(drainedStack)) {
                    stackToDrain = new FluidStack(drainedStack, maxDrain - FluidHelpers.getAmount(drainedStack));
                }
            } else {
                int amountDrained = FluidHelpers.getAmount(fluidHandler.drain(stackToDrain, action, ignoreInOutLimit));
                FluidHelpers.shrink(stackToDrain, amountDrained);
                FluidHelpers.grow(drainedStack, amountDrained);
                if (FluidHelpers.getAmount(drainedStack) == maxDrain) {
                    return drainedStack;
                }
            }
        }

        return drainedStack;
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean action, boolean ignoreInOutLimit) {
        int drained = 0;
        FluidStack toDrain = resource;
        for (IStorageFluidHandler fluidHandler : fluidHandlers) {
            drained += FluidHelpers.getAmount(fluidHandler.drain(toDrain, action, ignoreInOutLimit));
            if (drained == FluidHelpers.getAmount(resource)) {
                return resource;
            }
            toDrain = new FluidStack(toDrain, FluidHelpers.getAmount(resource) - drained);
        }

        return drained == 0 ? FluidHelpers.EMPTY : new FluidStack(resource, drained);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean action) {
        return drain(resource, action, false);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean action, boolean ignoreInOutLimit) {
        for (IStorageFluidHandler fluidHandler : fluidHandlers) {
            FluidStack drained = fluidHandler.drain(maxDrain, action, ignoreInOutLimit);
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

    @Override
    public ItemStack getContainer() {
        return backpack;
    }
}
