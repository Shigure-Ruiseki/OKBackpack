package ruiseki.storagecore.client.gui.sync;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.api.IPacketWriter;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.StoragePanel;

public class StorageSlotSH extends ItemSlotSH {

    private final IStorageWrapper wrapper;
    private final StoragePanel panel;

    public StorageSlotSH(ModularSlot slot, IStorageWrapper wrapper, StoragePanel panel) {
        super(slot);
        this.wrapper = wrapper;
        this.panel = panel;
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == SYNC_ITEM || id == SYNC_ENABLED) {
            super.readOnClient(id, buf);
            return;
        }
        try {
            StorageSHRegistry.handleClient(this, id, buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == SYNC_ITEM || id == SYNC_ENABLED) {
            super.readOnServer(id, buf);
            return;
        }
        try {
            StorageSHRegistry.handleServer(this, id, buf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public IStorageWrapper getWrapper() {
        return wrapper;
    }

    public StoragePanel getPanel() {
        return panel;
    }
}
