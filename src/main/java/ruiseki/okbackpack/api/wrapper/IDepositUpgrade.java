package ruiseki.okbackpack.api.wrapper;

/**
 * Deposit upgrade marker interface. Transfers items from backpack to target container.
 */
public interface IDepositUpgrade extends IInventoryInteractionUpgrade {

    default boolean canDeposit() {
        return true;
    }
}
