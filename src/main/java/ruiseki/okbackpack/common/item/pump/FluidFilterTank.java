package ruiseki.okbackpack.common.item.pump;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;

/**
 * Phantom tank backing one filter slot of an {@link AdvancedPumpUpgradeWrapper}. Capacity is
 * effectively infinite – it only holds a fluid identity used by the filter logic. Mutation
 * delegates back to the wrapper which writes to NBT. The upgrade wrapper is resolved
 * dynamically from the storage wrapper + upgrade slot index so this works on both client and
 * server without explicit binding.
 */
public class FluidFilterTank implements IFluidTank {

    private final IStorageWrapper storage;
    private final int upgradeSlotIndex;
    private final int index;

    public FluidFilterTank(IStorageWrapper storage, int upgradeSlotIndex, int index) {
        this.storage = storage;
        this.upgradeSlotIndex = upgradeSlotIndex;
        this.index = index;
    }

    private AdvancedPumpUpgradeWrapper resolveWrapper() {
        IUpgradeWrapper w = storage.getUpgradeHandler()
            .getWrapperInSlot(upgradeSlotIndex);
        return (w instanceof AdvancedPumpUpgradeWrapper adv) ? adv : null;
    }

    @Override
    public FluidStack getFluid() {
        AdvancedPumpUpgradeWrapper w = resolveWrapper();
        return w == null ? null : w.getFilterFluid(index);
    }

    @Override
    public int getFluidAmount() {
        FluidStack f = getFluid();
        return f == null ? 0 : f.amount;
    }

    @Override
    public int getCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public FluidTankInfo getInfo() {
        return new FluidTankInfo(getFluid(), getCapacity());
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.getFluid() == null) return 0;
        AdvancedPumpUpgradeWrapper w = resolveWrapper();
        if (w == null) return 0;
        if (doFill) {
            w.setFilterFluid(index, resource.copy());
        }
        return resource.amount;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        AdvancedPumpUpgradeWrapper w = resolveWrapper();
        if (w == null) return null;
        FluidStack cur = w.getFilterFluid(index);
        if (cur == null) return null;
        FluidStack out = cur.copy();
        if (doDrain) {
            w.setFilterFluid(index, null);
        }
        return out;
    }
}
