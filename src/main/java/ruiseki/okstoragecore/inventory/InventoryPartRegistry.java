package ruiseki.okstoragecore.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import ruiseki.okstoragecore.settings.memory.MemorySettingsCategory;

public class InventoryPartRegistry {

    private InventoryPartRegistry() {}

    private static final Map<String, IInventoryPartHandler.Factory> INVENTORY_PART_FACTORIES = new HashMap<>();

    static {
        registerFactory(
            IInventoryPartHandler.Default.NAME,
            (parent, slotRange,
                getMemorySettings) -> new IInventoryPartHandler.Default(parent, slotRange.numberOfSlots()));
    }

    public static void registerFactory(String name, IInventoryPartHandler.Factory factory) {
        INVENTORY_PART_FACTORIES.put(name, factory);
    }

    public static IInventoryPartHandler instantiatePart(String name, InventoryHandler parent,
        InventoryPartitioner.SlotRange slotRange, Supplier<MemorySettingsCategory> getMemorySettings) {
        return INVENTORY_PART_FACTORIES.get(name)
            .create(parent, slotRange, getMemorySettings);
    }
}
