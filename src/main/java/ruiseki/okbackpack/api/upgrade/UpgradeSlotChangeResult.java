package ruiseki.okbackpack.api.upgrade;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;

public class UpgradeSlotChangeResult {

    private static final int[] EMPTY_SLOTS = new int[0];
    private static final UpgradeSlotChangeResult SUCCESS = new UpgradeSlotChangeResult(true, null, EMPTY_SLOTS);

    @Getter
    private final boolean successful;
    @Nullable
    private final String errorLangKey;
    @Getter
    private final Object[] errorArgs;
    @Getter
    private final int[] conflictSlots;

    private UpgradeSlotChangeResult(boolean successful, @Nullable String errorLangKey, int[] conflictSlots,
        Object... errorArgs) {
        this.successful = successful;
        this.errorLangKey = errorLangKey;
        this.conflictSlots = conflictSlots;
        this.errorArgs = errorArgs;
    }

    @Nullable
    public String getErrorLangKey() {
        return errorLangKey;
    }

    public static UpgradeSlotChangeResult success() {
        return SUCCESS;
    }

    public static UpgradeSlotChangeResult fail(String errorLangKey, int[] conflictSlots, Object... args) {
        return new UpgradeSlotChangeResult(false, errorLangKey, conflictSlots, args);
    }
}
