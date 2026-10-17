package ruiseki.storagecore.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import ruiseki.okbackpack.Reference;
import ruiseki.storagecore.settings.memory.MemorySettingsCategory;

public class StorageSlotSHRegistries {

    public static final StorageSHRegistry.Entry<StorageSlotSH> REGISTRY = StorageSHRegistry.get(StorageSlotSH.class);

    public static final ResourceLocation SET_MEMORY_SLOT = new ResourceLocation(
        Reference.MOD_ID,
        "storage_set_memory_slot");
    public static final ResourceLocation UNSET_MEMORY_SLOT = new ResourceLocation(
        Reference.MOD_ID,
        "storage_unset_memory_slot");

    public static void load() {

        REGISTRY.registerServer(SET_MEMORY_SLOT, (handler, buf) -> {
            MemorySettingsCategory memory = getMemorySettings(handler);
            memory.selectSlot(handler.getSlotIndex());
        });
        REGISTRY.registerServer(UNSET_MEMORY_SLOT, (handler, buf) -> {
            MemorySettingsCategory memory = getMemorySettings(handler);
            memory.unselectSlot(handler.getSlotIndex());
        });
    }

    public static MemorySettingsCategory getMemorySettings(StorageSlotSH slotSH) {
        return slotSH.getWrapper()
            .getSettingsHandler()
            .getTypeCategory(MemorySettingsCategory.class);
    }
}
