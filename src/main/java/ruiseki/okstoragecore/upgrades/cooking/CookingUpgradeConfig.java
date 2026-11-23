package ruiseki.okstoragecore.upgrades.cooking;

import java.util.function.DoubleSupplier;

public class CookingUpgradeConfig {

    public final DoubleSupplier cookingSpeedMultiplier;
    public final DoubleSupplier fuelEfficiencyMultiplier;

    public CookingUpgradeConfig(DoubleSupplier cookingSpeedMultiplier, DoubleSupplier fuelEfficiencyMultiplier) {
        this.cookingSpeedMultiplier = cookingSpeedMultiplier;
        this.fuelEfficiencyMultiplier = fuelEfficiencyMultiplier;
    }

    public double getCookingSpeedMultiplier() {
        return cookingSpeedMultiplier.getAsDouble();
    }

    public double getFuelEfficiencyMultiplier() {
        return fuelEfficiencyMultiplier.getAsDouble();
    }
}
