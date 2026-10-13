package ruiseki.storagecore.client.gui.sync;

import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.network.NetworkUtils;

import ruiseki.okbackpack.Reference;
import ruiseki.okcore.inventory.SortBy;

public class StorageSHRegisters {

    public static final StorageSHRegistry.Entry<StorageSH> REGISTRY = StorageSHRegistry.get(StorageSH.class);

    public static final ResourceLocation SORT = new ResourceLocation(Reference.MOD_ID, "storage_sort");
    public static final ResourceLocation SORT_BY = new ResourceLocation(Reference.MOD_ID, "storage_sort_by");

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
    }

}
