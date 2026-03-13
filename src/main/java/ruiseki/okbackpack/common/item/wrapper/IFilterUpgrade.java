package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

public interface IFilterUpgrade {

    String FILTER_WAY_TAG = "FilterWay";

    FilterWayType getfilterWay();

    void setFilterWay(FilterWayType filterWay);

    boolean canInsert(ItemStack stack);

    boolean canExtract(ItemStack stack);

    enum FilterWayType {
        IN_OUT,
        IN,
        OUT;
    }
}
