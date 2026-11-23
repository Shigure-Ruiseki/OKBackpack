package ruiseki.okbackpack.upgrades.toolswapper;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ruiseki.okcore.datastructure.StringRepresentable;

public enum ToolSwapMode implements StringRepresentable {

    ANY("name"),
    ONLY_TOOLS("onlyTools"),
    NO_SWAP("noSwap");

    private final String name;

    ToolSwapMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public ToolSwapMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    private static final ToolSwapMode[] VALUES;
    private static final Map<String, ToolSwapMode> NAME_VALUES;

    static {
        ImmutableMap.Builder<String, ToolSwapMode> builder = new ImmutableMap.Builder<>();
        for (ToolSwapMode value : ToolSwapMode.values()) {
            builder.put(value.getSerializedName(), value);
        }
        NAME_VALUES = builder.build();
        VALUES = values();
    }

    public static ToolSwapMode fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) {
            return ANY;
        }
        return VALUES[ordinal];
    }

    public static ToolSwapMode fromName(String name) {
        return NAME_VALUES.getOrDefault(name, ANY);
    }
}
