package ruiseki.storagecore.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.network.NetworkUtils;

import ruiseki.okbackpack.Reference;
import ruiseki.okcore.inventory.SortBy;
import ruiseki.storagecore.helper.StorageInventoryHelpers;

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
    }

}
