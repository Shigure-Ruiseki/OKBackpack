package ruiseki.storagecore.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.network.NetworkUtils;

import ruiseki.okbackpack.Reference;
import ruiseki.okcore.inventory.SortBy;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.helper.StorageInventoryHelpers;
import ruiseki.storagecore.settings.memory.MemorySettingsCategory;
import ruiseki.storagecore.settings.nosort.NoSortSettingsCategory;

public class StorageSHRegisters {

    public static final StorageSHRegistry.Entry<StorageSH> REGISTRY = StorageSHRegistry.get(StorageSH.class);

    public static final ResourceLocation SORT = new ResourceLocation(Reference.MOD_ID, "storage_sort");
    public static final ResourceLocation SORT_BY = new ResourceLocation(Reference.MOD_ID, "storage_sort_by");
    public static final ResourceLocation TRANSFER_TO_STORAGE = new ResourceLocation(
        Reference.MOD_ID,
        "storage_transfer_to_storage");
    public static final ResourceLocation TRANSFER_TO_PLAYER = new ResourceLocation(
        Reference.MOD_ID,
        "storage_transfer_to_player");
    public static final ResourceLocation OPENED_ID = new ResourceLocation(Reference.MOD_ID, "storage_opended_id");
    public static final ResourceLocation TAB_INDEX = new ResourceLocation(Reference.MOD_ID, "storage_tab_index");

    public static final ResourceLocation SET_MEMORY_SLOT = new ResourceLocation(
        Reference.MOD_ID,
        "storage_set_memory_slot");
    public static final ResourceLocation UNSET_MEMORY_SLOT = new ResourceLocation(
        Reference.MOD_ID,
        "storage_unset_memory_slot");
    public static final ResourceLocation SET_NO_SORT_SLOT = new ResourceLocation(
        Reference.MOD_ID,
        "storage_set_no_sort_slot");
    public static final ResourceLocation UNSET_NO_SORT_SLOT = new ResourceLocation(
        Reference.MOD_ID,
        "storage_unset_no_sort_slot");
    public static final ResourceLocation SET_MEMORY_ALL_SLOT = new ResourceLocation(
        Reference.MOD_ID,
        "storage_set_memory_all_slot");
    public static final ResourceLocation UNSET_MEMORY_ALL_SLOT = new ResourceLocation(
        Reference.MOD_ID,
        "storage_unset_memory_all_slot");
    public static final ResourceLocation SET_IGNORE_NBT = new ResourceLocation(
        Reference.MOD_ID,
        "storage_set_ignore_nbt");
    public static final ResourceLocation SET_NO_SORT_ALL_SLOT = new ResourceLocation(
        Reference.MOD_ID,
        "storage_set_no_sort_all_slot");
    public static final ResourceLocation UNSET_NO_SORT_ALL_SLOT = new ResourceLocation(
        Reference.MOD_ID,
        "storage_unset_no_sort_all_slot");
    public static final ResourceLocation SET_NO_SORT_SLOT_COLOR = new ResourceLocation(
        Reference.MOD_ID,
        "storage_unset_no_sort_slot_color");

    public static void load() {

        REGISTRY.registerServer(
            SORT,
            (handler, buf) -> {
                handler.getWrapper()
                    .sort();
            });
        REGISTRY.registerServer(SORT_BY, (handler, buf) -> {
            SortBy sortBy = NetworkUtils.readEnumValue(buf, SortBy.class);
            handler.getWrapper()
                .setSortBy(sortBy);
        });
        REGISTRY.registerServer(TRANSFER_TO_STORAGE, (handler, buf) -> {
            boolean transferMatched = buf.readBoolean();
            StorageInventoryHelpers
                .transferPlayerToStorage(handler.getWrapper(), handler.getPlayerInventory(), transferMatched);
        });
        REGISTRY.registerServer(TRANSFER_TO_PLAYER, (handler, buf) -> {
            boolean transferMatched = buf.readBoolean();
            StorageInventoryHelpers
                .transferStorageToPlayer(handler.getWrapper(), handler.getPlayerInventory(), transferMatched);
        });
        REGISTRY.registerServer(OPENED_ID, (handler, buf) -> {
            int openedId = buf.readInt();
            handler.getWrapper()
                .setOpenTabId(openedId);
        });
        REGISTRY.registerServer(TAB_INDEX, (handler, buf) -> {
            int tabIndex = buf.readInt();
            handler.getWrapper()
                .setTabIndex(tabIndex);
        });

        REGISTRY.registerServer(SET_MEMORY_SLOT, (handler, buf) -> {
            MemorySettingsCategory memory = getMemorySettings(handler.getWrapper());
            memory.selectSlot(buf.readInt());
        });
        REGISTRY.registerServer(UNSET_MEMORY_SLOT, (handler, buf) -> {
            MemorySettingsCategory memory = getMemorySettings(handler.getWrapper());
            memory.unselectSlot(buf.readInt());
        });
        REGISTRY.registerServer(SET_MEMORY_ALL_SLOT, (handler, buf) -> {
            MemorySettingsCategory settings = getMemorySettings(handler.getWrapper());
            settings.selectSlots(
                0,
                handler.getWrapper()
                    .getInventoryHandler()
                    .getSlots());
        });
        REGISTRY.registerServer(UNSET_MEMORY_ALL_SLOT, (handler, buf) -> {
            MemorySettingsCategory settings = getMemorySettings(handler.getWrapper());
            settings.unselectAllSlots();
        });
        REGISTRY.registerServer(SET_IGNORE_NBT, (handler, buf) -> {
            MemorySettingsCategory settings = getMemorySettings(handler.getWrapper());
            settings.setIgnoreNbt(buf.readBoolean());
        });

        REGISTRY.registerServer(SET_NO_SORT_SLOT, (handler, buf) -> {
            NoSortSettingsCategory settings = getNoSortingSettings(handler.getWrapper());
            settings.selectSlot(buf.readInt());
        });
        REGISTRY.registerServer(UNSET_NO_SORT_SLOT, (handler, buf) -> {
            NoSortSettingsCategory settings = getNoSortingSettings(handler.getWrapper());
            settings.unselectSlot(buf.readInt());
        });
        REGISTRY.registerServer(SET_NO_SORT_ALL_SLOT, (handler, buf) -> {
            NoSortSettingsCategory settings = getNoSortingSettings(handler.getWrapper());
            settings.selectSlots(
                0,
                handler.getWrapper()
                    .getInventoryHandler()
                    .getSlots());
        });
        REGISTRY.registerServer(UNSET_NO_SORT_ALL_SLOT, (handler, buf) -> {
            NoSortSettingsCategory settings = getNoSortingSettings(handler.getWrapper());
            settings.unselectAllSlots();
        });
        REGISTRY.registerServer(SET_NO_SORT_SLOT_COLOR, (handler, buf) -> {
            NoSortSettingsCategory settings = getNoSortingSettings(handler.getWrapper());
            settings.setColor(buf.readInt());
        });

    }

    public static MemorySettingsCategory getMemorySettings(IStorageWrapper wrapper) {
        return wrapper.getSettingsHandler()
            .getTypeCategory(MemorySettingsCategory.class);
    }

    public static NoSortSettingsCategory getNoSortingSettings(IStorageWrapper wrapper) {
        return wrapper.getSettingsHandler()
            .getTypeCategory(NoSortSettingsCategory.class);
    }
}
