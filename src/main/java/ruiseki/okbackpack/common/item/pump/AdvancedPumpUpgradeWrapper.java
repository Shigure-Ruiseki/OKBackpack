package ruiseki.okbackpack.common.item.pump;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class AdvancedPumpUpgradeWrapper extends PumpUpgradeWrapper {

    public static final int FILTER_SLOTS = 4;
    /** Tag name kept stable so old saves continue to load (now stores fluid list, not items). */
    public static final String FILTER_LIST_TAG = "PumpFluidFilters";
    public static final String FILTER_TYPE_TAG = "PumpFilterType";
    private static final int NBT_TAG_COMPOUND = 10;
    private static final String SLOT_KEY = "Slot";

    private final FluidStack[] fluidFilters = new FluidStack[FILTER_SLOTS];

    public AdvancedPumpUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer, true, true, true);
        loadFluidFilters();
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    public FluidStack getFilterFluid(int index) {
        if (index < 0 || index >= FILTER_SLOTS) return null;
        return fluidFilters[index];
    }

    public void setFilterFluid(int index, FluidStack fluid) {
        if (index < 0 || index >= FILTER_SLOTS) return;
        fluidFilters[index] = (fluid == null || fluid.getFluid() == null || fluid.amount <= 0) ? null : fluid;
        persistFluidFilters();
        save();
    }

    public FluidFilterType getFilterType() {
        int ordinal = ItemNBTHelpers.getInt(upgrade, FILTER_TYPE_TAG, FluidFilterType.WHITELIST.ordinal());
        return FluidFilterType.fromOrdinal(ordinal);
    }

    public void setFilterType(FluidFilterType type) {
        ItemNBTHelpers.setInt(upgrade, FILTER_TYPE_TAG, type.ordinal());
        save();
    }

    @Override
    public boolean passesFluidFilter(FluidStack fluidStack) {
        if (fluidStack == null || fluidStack.getFluid() == null) return true;
        boolean anyConfigured = false;
        boolean matched = false;
        for (FluidStack filter : fluidFilters) {
            if (filter == null || filter.getFluid() == null) continue;
            anyConfigured = true;
            if (filter.getFluid() == fluidStack.getFluid()) {
                matched = true;
                break;
            }
        }
        if (!anyConfigured) return true;
        return (getFilterType() == FluidFilterType.WHITELIST) == matched;
    }

    private void loadFluidFilters() {
        NBTTagCompound nbt = ItemNBTHelpers.getNBT(upgrade);
        if (!nbt.hasKey(FILTER_LIST_TAG, 9)) return;
        NBTTagList list = nbt.getTagList(FILTER_LIST_TAG, NBT_TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            int idx = entry.getInteger(SLOT_KEY);
            if (idx < 0 || idx >= FILTER_SLOTS) continue;
            fluidFilters[idx] = FluidStack.loadFluidStackFromNBT(entry);
        }
    }

    private void persistFluidFilters() {
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < FILTER_SLOTS; i++) {
            FluidStack f = fluidFilters[i];
            if (f == null) continue;
            NBTTagCompound entry = new NBTTagCompound();
            f.writeToNBT(entry);
            entry.setInteger(SLOT_KEY, i);
            list.appendTag(entry);
        }
        NBTTagCompound nbt = ItemNBTHelpers.getNBT(upgrade);
        nbt.setTag(FILTER_LIST_TAG, list);
    }
}
