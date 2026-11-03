package ruiseki.okstoragecore.upgrades.magnet;

import java.util.function.IntSupplier;

import ruiseki.okstoragecore.upgrades.FilteredUpgradeConfigBase;

public class MagnetUpgradeConfig extends FilteredUpgradeConfigBase {

    public final IntSupplier magnetRange;

    public MagnetUpgradeConfig(IntSupplier magnetRange, IntSupplier filterSlots, IntSupplier slotsInRow) {
        super(filterSlots, slotsInRow);
        this.magnetRange = magnetRange;
    }

    public int getMagnetRange() {
        return magnetRange.getAsInt();
    }
}
