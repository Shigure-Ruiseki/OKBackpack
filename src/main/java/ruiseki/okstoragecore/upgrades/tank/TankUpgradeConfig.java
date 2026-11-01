package ruiseki.okstoragecore.upgrades.tank;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public class TankUpgradeConfig {

    public final IntSupplier capacityPerSlotRow;
    public final DoubleSupplier stackMultiplierRatio;
    public final IntSupplier autoFillDrainContainerCooldown;
    public final IntSupplier maxInputOutput;

    public TankUpgradeConfig(IntSupplier capacityPerSlotRow, DoubleSupplier stackMultiplierRatio,
        IntSupplier autoFillDrainContainerCooldown, IntSupplier maxInputOutput) {
        this.capacityPerSlotRow = capacityPerSlotRow;
        this.stackMultiplierRatio = stackMultiplierRatio;
        this.autoFillDrainContainerCooldown = autoFillDrainContainerCooldown;
        this.maxInputOutput = maxInputOutput;
    }

    public double getStackMultiplierRatio() {
        return stackMultiplierRatio.getAsDouble();
    }

    public int getAutoFillDrainContainerCooldown() {
        return autoFillDrainContainerCooldown.getAsInt();
    }

    public int getCapacityPerSlotRow() {
        return capacityPerSlotRow.getAsInt();
    }

    public int getMaxInputOutput() {
        return maxInputOutput.getAsInt();
    }
}
