package ruiseki.okstoragecore.upgrades;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ruiseki.okcore.datastructure.StringRepresentable;

public enum PrimaryMatch implements StringRepresentable {

    ITEM("item"),
    MOD("mod"),
    ORE_DICT("ore_dict");

    private final String name;

    PrimaryMatch(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public PrimaryMatch next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    private static final Map<String, PrimaryMatch> NAME_VALUES;
    private static final PrimaryMatch[] VALUES;

    static {
        ImmutableMap.Builder<String, PrimaryMatch> builder = new ImmutableMap.Builder<>();
        for (PrimaryMatch value : PrimaryMatch.values()) {
            builder.put(value.getSerializedName(), value);
        }
        NAME_VALUES = builder.build();
        VALUES = values();
    }

    public static PrimaryMatch fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) {
            return ITEM;
        }
        return VALUES[ordinal];
    }

    public static PrimaryMatch fromName(String name) {
        return NAME_VALUES.getOrDefault(name, ITEM);
    }
}
