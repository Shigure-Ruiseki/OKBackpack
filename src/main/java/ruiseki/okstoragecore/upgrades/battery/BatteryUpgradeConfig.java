package ruiseki.okstoragecore.upgrades.battery;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public class BatteryUpgradeConfig {

    public final IntSupplier energyPerSlotRow;
    public final DoubleSupplier stackMultiplierRatio;
    public final IntSupplier maxInputOutput;

    public BatteryUpgradeConfig(IntSupplier energyPerSlotRow, DoubleSupplier stackMultiplierRatio,
        IntSupplier maxInputOutput) {
        this.energyPerSlotRow = energyPerSlotRow;
        this.stackMultiplierRatio = stackMultiplierRatio;
        this.maxInputOutput = maxInputOutput;
    }

    public double getStackMultiplierRatio() {
        return stackMultiplierRatio.getAsDouble();
    }

    public int getEnergyPerSlotRow() {
        return energyPerSlotRow.getAsInt();
    }

    public int getMaxInputOutput() {
        return maxInputOutput.getAsInt();
    }
}
