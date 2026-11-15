package ruiseki.okstoragecore.upgrades.jukebox;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ruiseki.okcore.datastructure.StringRepresentable;

public enum RepeatMode implements StringRepresentable {

    ALL("all"),
    ONE("one"),
    NO("no");

    private final String name;

    RepeatMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public RepeatMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    private static final Map<String, RepeatMode> NAME_VALUES;
    private static final RepeatMode[] VALUES;

    static {
        ImmutableMap.Builder<String, RepeatMode> builder = new ImmutableMap.Builder<>();
        for (RepeatMode value : RepeatMode.values()) {
            builder.put(value.getSerializedName(), value);
        }
        NAME_VALUES = builder.build();
        VALUES = values();
    }

    public static RepeatMode fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) {
            return ALL;
        }
        return VALUES[ordinal];
    }

    public static RepeatMode fromName(String name) {
        return NAME_VALUES.getOrDefault(name, NO);
    }
}
