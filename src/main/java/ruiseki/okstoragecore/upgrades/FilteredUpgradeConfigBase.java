package ruiseki.okstoragecore.upgrades;

import java.util.function.IntSupplier;

public class FilteredUpgradeConfigBase {

    public final IntSupplier filterSlots;
    public final IntSupplier slotsInRow;

    public FilteredUpgradeConfigBase(IntSupplier filterSlots, IntSupplier slotsInRow) {
        this.filterSlots = filterSlots;
        this.slotsInRow = slotsInRow;
    }

    public int getFilterSlots() {
        return filterSlots.getAsInt();
    }

    public int getSlotsInRow() {
        return slotsInRow.getAsInt();
    }
}
