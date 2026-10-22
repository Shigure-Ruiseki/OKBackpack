package ruiseki.okstoragecore.client.gui.sync;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.api.IPacketWriter;
import com.cleanroommc.modularui.value.sync.SyncHandler;

public class SyncHandlerBase<T extends SyncHandlerBase<T>> extends SyncHandler<T> {

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        StorageSHRegistry.handleClient(this, id, buf);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        StorageSHRegistry.handleServer(this, id, buf);
    }

    @Override
    public boolean isAllowC2S() {
        return true;
    }

    public void syncToServer(ResourceLocation id) {
        syncToServer(StorageSHRegistry.getId(id), buffer -> {});
    }

    public void syncToClient(ResourceLocation id) {
        syncToClient(StorageSHRegistry.getId(id), buffer -> {});
    }

    public void syncToServer(ResourceLocation id, IPacketWriter writer) {
        syncToServer(StorageSHRegistry.getId(id), writer);
    }

    public void syncToClient(ResourceLocation id, IPacketWriter writer) {
        syncToClient(StorageSHRegistry.getId(id), writer);
    }

    public void syncToBoth(ResourceLocation id, IPacketWriter writer) {
        syncToServer(id, writer);
        syncToClient(id, writer);
    }
}
