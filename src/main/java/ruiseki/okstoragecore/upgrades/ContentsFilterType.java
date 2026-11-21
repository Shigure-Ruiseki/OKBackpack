package ruiseki.okstoragecore.upgrades;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ruiseki.okcore.datastructure.StringRepresentable;

public enum ContentsFilterType implements StringRepresentable {

    ALLOW("allow"),
    BLOCK("block"),
    STORAGE("storage");

    private final String name;

    ContentsFilterType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public ContentsFilterType next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    private static final Map<String, ContentsFilterType> NAME_VALUES;
    private static final ContentsFilterType[] VALUES;

    static {
        ImmutableMap.Builder<String, ContentsFilterType> builder = new ImmutableMap.Builder<>();
        for (ContentsFilterType value : values()) {
            builder.put(value.getSerializedName(), value);
        }
        NAME_VALUES = builder.build();
        VALUES = values();
    }

    public static ContentsFilterType fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) {
            return ALLOW;
        }
        return VALUES[ordinal];
    }

    public static ContentsFilterType fromName(String name) {
        return NAME_VALUES.getOrDefault(name, BLOCK);
    }
}
