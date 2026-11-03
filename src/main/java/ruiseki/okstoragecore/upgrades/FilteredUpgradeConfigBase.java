package ruiseki.okstoragecore.upgrades;

import java.util.function.IntSupplier;

public class FilteredUpgradeConfigBase {

    public final IntSupplier filterSlots;

    public FilteredUpgradeConfigBase(IntSupplier filterSlots) {
        this.filterSlots = filterSlots;
    }

    public int getFilterSlots() {
        return filterSlots.getAsInt();
    }
}
