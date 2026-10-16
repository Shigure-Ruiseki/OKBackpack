package ruiseki.storagecore.upgrades;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

public class UpgradeSlotChangeResult {

    private final boolean successful;
    @Nullable
    private final String errorMessage;
    private final Set<Integer> errorUpgradeSlots;
    private final Set<Integer> errorInventorySlots;
    private final Set<Integer> errorInventoryParts;

    private UpgradeSlotChangeResult(boolean successful, @Nullable String errorMessage, Set<Integer> errorUpgradeSlots,
        Set<Integer> errorInventorySlots, Set<Integer> errorInventoryParts) {
        this.successful = successful;
        this.errorMessage = errorMessage;
        this.errorUpgradeSlots = errorUpgradeSlots;
        this.errorInventorySlots = errorInventorySlots;
        this.errorInventoryParts = errorInventoryParts;
    }

    public Set<Integer> getErrorInventorySlots() {
        return errorInventorySlots;
    }

    public Set<Integer> getErrorInventoryParts() {
        return errorInventoryParts;
    }

    public static class Fail extends UpgradeSlotChangeResult {

        public Fail(String errorMessage, Set<Integer> errorUpgradeSlots, Set<Integer> errorInventorySlots,
            Set<Integer> errorInventoryParts) {
            super(false, errorMessage, errorUpgradeSlots, errorInventorySlots, errorInventoryParts);
        }
    }

    public static class Success extends UpgradeSlotChangeResult {

        public Success() {
            super(true, null, Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        }
    }

    public boolean isSuccessful() {
        return successful;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public Set<Integer> getErrorUpgradeSlots() {
        return errorUpgradeSlots;
    }
}
