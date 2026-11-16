package ruiseki.okstoragecore.upgrades.cooking;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public class AutoCookingUpgradeConfig extends CookingUpgradeConfig {

    public final IntSupplier inputFilterSlots;
    public final IntSupplier inputFilterSlotsInRow;
    public final IntSupplier fuelFilterSlots;
    public final IntSupplier fuelFilterSlotsInRow;

    public AutoCookingUpgradeConfig(DoubleSupplier cookingSpeedMultiplier, DoubleSupplier fuelEfficiencyMultiplier,
        IntSupplier inputFilterSlots, IntSupplier inputFilterSlotsInRow, IntSupplier fuelFilterSlots,
        IntSupplier fuelFilterSlotsInRow) {
        super(cookingSpeedMultiplier, fuelEfficiencyMultiplier);
        this.inputFilterSlots = inputFilterSlots;
        this.inputFilterSlotsInRow = inputFilterSlotsInRow;
        this.fuelFilterSlots = fuelFilterSlots;
        this.fuelFilterSlotsInRow = fuelFilterSlotsInRow;
    }

    public int getInputFilterSlots() {
        return inputFilterSlots.getAsInt();
    }

    public int getInputFilterSlotsInRow() {
        return inputFilterSlotsInRow.getAsInt();
    }

    public int getFuelFilterSlots() {
        return fuelFilterSlots.getAsInt();
    }

    public int getFuelFilterSlotsInRow() {
        return fuelFilterSlotsInRow.getAsInt();
    }
}
