package ruiseki.okbackpack.common.item.pump;

/**
 * Filter mode used by the advanced pump upgrade.
 */
public enum FluidFilterType {

    WHITELIST("gui.backpack.pump.filter_whitelist"),
    BLACKLIST("gui.backpack.pump.filter_blacklist");

    private final String langKey;

    FluidFilterType(String langKey) {
        this.langKey = langKey;
    }

    public String getLangKey() {
        return langKey;
    }

    public FluidFilterType next() {
        FluidFilterType[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static FluidFilterType fromOrdinal(int ordinal) {
        FluidFilterType[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return WHITELIST;
        }
        return values[ordinal];
    }
}
