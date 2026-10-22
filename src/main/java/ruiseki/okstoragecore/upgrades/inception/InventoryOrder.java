package ruiseki.okstoragecore.upgrades.inception;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import ruiseki.okcore.datastructure.StringRepresentable;

public enum InventoryOrder implements StringRepresentable {

    MAIN_FIRST("main_first"),
    INCEPTED_FIRST("incepted_first");

    private final String name;

    InventoryOrder(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public InventoryOrder next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    private static final Map<String, InventoryOrder> NAME_VALUES;
    private static final InventoryOrder[] VALUES;

    static {
        ImmutableMap.Builder<String, InventoryOrder> builder = new ImmutableMap.Builder<>();
        for (InventoryOrder value : InventoryOrder.values()) {
            builder.put(value.getSerializedName(), value);
        }
        NAME_VALUES = builder.build();
        VALUES = values();
    }

    public static InventoryOrder fromName(String name) {
        return NAME_VALUES.getOrDefault(name, MAIN_FIRST);
    }
}
