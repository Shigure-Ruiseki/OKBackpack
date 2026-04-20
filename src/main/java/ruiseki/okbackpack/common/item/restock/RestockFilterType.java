package ruiseki.okbackpack.common.item.restock;

public enum RestockFilterType {

    ALLOW,
    BLOCK,
    STORAGE;

    private static final RestockFilterType[] VALUES = values();

    public RestockFilterType next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }
}
