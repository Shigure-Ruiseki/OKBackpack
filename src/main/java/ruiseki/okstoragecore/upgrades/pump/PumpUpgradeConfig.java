package ruiseki.okstoragecore.upgrades.pump;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public class PumpUpgradeConfig {

    public final IntSupplier maxInputOutput;
    public final DoubleSupplier stackMultiplierRatio;
    public final IntSupplier filterSlots;

    public PumpUpgradeConfig(IntSupplier maxInputOutput, DoubleSupplier stackMultiplierRatio, IntSupplier filterSlots) {
        this.maxInputOutput = maxInputOutput;
        this.stackMultiplierRatio = stackMultiplierRatio;
        this.filterSlots = filterSlots;
    }

    public int getFilterSlots() {
        return filterSlots.getAsInt();
    }

    public int getMaxInputOutput() {
        return maxInputOutput.getAsInt();
    }

    public double getStackMultiplierRatio() {
        return stackMultiplierRatio.getAsDouble();
    }
}
