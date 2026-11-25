package ruiseki.okbackpack.api.wrapper;

/**
 * Deposit upgrade marker interface. Transfers items from backpack to target container.
 */
public interface IDepositUpgrade extends IInventoryInteractionUpgrade {

    String DEPOSIT_FILTER_TYPE_TAG = "DepositFilterType";

    default boolean canDeposit() {
        return true;
    }

    DepositFilterType getDepositFilterType();

    void setDepositFilterType(DepositFilterType type);

    enum DepositFilterType {

        ALLOW,
        BLOCK,
        INVENTORY;

        private static final DepositFilterType[] VALUES = values();

        public DepositFilterType next() {
            return VALUES[(ordinal() + 1) % VALUES.length];
        }
    }
}
