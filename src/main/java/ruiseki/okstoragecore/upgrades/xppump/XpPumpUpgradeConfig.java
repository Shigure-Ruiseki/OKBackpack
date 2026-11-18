package ruiseki.okstoragecore.upgrades.xppump;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

import ruiseki.okstoragecore.helper.XpHelpers;

public class XpPumpUpgradeConfig {

    public final IntSupplier maxXpPointsPerMending;
    public final BooleanSupplier mendingOn;

    public XpPumpUpgradeConfig(IntSupplier maxXpPointsPerMending, BooleanSupplier mendingOn) {
        this.maxXpPointsPerMending = maxXpPointsPerMending;
        this.mendingOn = mendingOn;
    }

    public boolean isMendingOn() {
        return mendingOn.getAsBoolean() && XpHelpers.isMendingEnabled();
    }

    public int getMaxXpPointsPerMending() {
        return maxXpPointsPerMending.getAsInt();
    }
}
