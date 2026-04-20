package ruiseki.okbackpack.common.item.deposit;

public enum DepositFilterType {

    ALLOW,
    BLOCK,
    INVENTORY;

    private static final DepositFilterType[] VALUES = values();

    public DepositFilterType next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }
}
