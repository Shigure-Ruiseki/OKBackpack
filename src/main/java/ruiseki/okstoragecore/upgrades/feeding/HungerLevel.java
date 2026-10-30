package ruiseki.okstoragecore.upgrades.feeding;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ruiseki.okcore.datastructure.StringRepresentable;

public enum HungerLevel implements StringRepresentable {

    ANY("any"),
    HALF("half"),
    FULL("full");

    private final String name;

    HungerLevel(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public HungerLevel next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    private static final Map<String, HungerLevel> NAME_VALUES;
    private static final HungerLevel[] VALUES;

    static {
        ImmutableMap.Builder<String, HungerLevel> builder = new ImmutableMap.Builder<>();
        for (HungerLevel value : HungerLevel.values()) {
            builder.put(value.getSerializedName(), value);
        }
        NAME_VALUES = builder.build();
        VALUES = values();
    }

    public static HungerLevel fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) {
            return ANY;
        }
        return VALUES[ordinal];
    }

    public static HungerLevel fromName(String name) {
        return NAME_VALUES.getOrDefault(name, HALF);
    }
}
