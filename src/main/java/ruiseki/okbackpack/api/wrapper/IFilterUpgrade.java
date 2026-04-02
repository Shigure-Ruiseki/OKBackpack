package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public interface IFilterUpgrade {

    String FILTER_WAY_TAG = "FilterWay";

    FilterWayType getfilterWay();

    void setFilterWay(FilterWayType filterWay);

    /**
     * Determines whether an item can be inserted into the given slot.
     *
     * @param slot  the index of the slot
     * @param stack the ItemStack to check
     * @return true if insertion is allowed, false otherwise
     */
    default boolean canInsert(int slot, @Nullable ItemStack stack) {
        return true; // default allows all insertions
    }

    /**
     * Determines whether an item can be extracted from the given slot.
     *
     * @param slot  the index of the slot
     * @param stack the ItemStack to check
     * @return true if extraction is allowed, false otherwise
     */
    default boolean canExtract(int slot, @Nullable ItemStack stack) {
        return true; // default allows all extractions
    }

    enum FilterWayType {
        IN_OUT,
        IN,
        OUT;
    }
}
