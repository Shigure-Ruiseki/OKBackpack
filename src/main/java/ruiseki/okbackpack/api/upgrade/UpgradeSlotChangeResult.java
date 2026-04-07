package ruiseki.okbackpack.api.upgrade;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;

public class UpgradeSlotChangeResult {

    private static final int[] EMPTY_SLOTS = new int[0];
    private static final UpgradeSlotChangeResult SUCCESS = new UpgradeSlotChangeResult(
        true,
        null,
        EMPTY_SLOTS,
        EMPTY_SLOTS);

    @Getter
    private final boolean successful;
    @Nullable
    private final String errorLangKey;
    @Getter
    private final Object[] errorArgs;
    @Getter
    private final int[] conflictSlots;
    @Getter
    private final int[] inventoryConflictSlots;

    private UpgradeSlotChangeResult(boolean successful, @Nullable String errorLangKey, int[] conflictSlots,
        int[] inventoryConflictSlots, Object... errorArgs) {
        this.successful = successful;
        this.errorLangKey = errorLangKey;
        this.conflictSlots = conflictSlots;
        this.inventoryConflictSlots = inventoryConflictSlots;
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
        return new UpgradeSlotChangeResult(false, errorLangKey, conflictSlots, EMPTY_SLOTS, args);
    }

    public static UpgradeSlotChangeResult failWithInventoryConflicts(String errorLangKey, int[] inventoryConflictSlots,
        Object... args) {
        return new UpgradeSlotChangeResult(false, errorLangKey, EMPTY_SLOTS, inventoryConflictSlots, args);
    }

    /**
     * %s cannot be installed on %s
     */
    public static UpgradeSlotChangeResult failUpgradeNotAllowed(int[] conflictSlots, String upgradeName,
        String storageName) {
        return fail("gui.backpack.error.add.upgrade_not_allowed", conflictSlots, upgradeName, storageName);
    }

    /**
     * Only a single %s can be installed on %s
     */
    public static UpgradeSlotChangeResult failOnlySingleAllowed(int[] conflictSlots, String upgradeName,
        String storageName) {
        return fail("gui.backpack.error.add.only_single_upgrade_allowed", conflictSlots, upgradeName, storageName);
    }

    /**
     * Only %d %s can be installed on %s
     */
    public static UpgradeSlotChangeResult failOnlyXAllowed(int[] conflictSlots, int maxCount, String upgradeName,
        String storageName) {
        return fail(
            "gui.backpack.error.add.only_x_upgrades_allowed",
            conflictSlots,
            maxCount,
            upgradeName,
            storageName);
    }

    /**
     * Stack multiplier must be greater than %s
     */
    public static UpgradeSlotChangeResult failStackLowMultiplier(int[] inventoryConflictSlots,
        String formattedMultiplier) {
        return failWithInventoryConflicts(
            "gui.backpack.error.remove.stack_low_multiplier",
            inventoryConflictSlots,
            formattedMultiplier);
    }
}
