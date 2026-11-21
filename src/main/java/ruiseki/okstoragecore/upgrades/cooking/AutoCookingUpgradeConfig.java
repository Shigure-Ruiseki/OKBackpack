package ruiseki.okstoragecore.upgrades.cooking;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public class AutoCookingUpgradeConfig extends CookingUpgradeConfig {

    public final IntSupplier inputFilterSlots;
    public final IntSupplier fuelFilterSlots;

    public AutoCookingUpgradeConfig(DoubleSupplier cookingSpeedMultiplier, DoubleSupplier fuelEfficiencyMultiplier,
        IntSupplier inputFilterSlots, IntSupplier fuelFilterSlots) {
        super(cookingSpeedMultiplier, fuelEfficiencyMultiplier);
        this.inputFilterSlots = inputFilterSlots;
        this.fuelFilterSlots = fuelFilterSlots;
    }

    public int getInputFilterSlots() {
        return inputFilterSlots.getAsInt();
    }

    public int getFuelFilterSlots() {
        return fuelFilterSlots.getAsInt();
    }
}
