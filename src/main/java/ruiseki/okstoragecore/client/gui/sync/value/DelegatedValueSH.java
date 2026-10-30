package ruiseki.okstoragecore.client.gui.sync.value;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import com.cleanroommc.modularui.api.IPacketWriter;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.sync.StorageSHRegistry;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public abstract class DelegatedValueSH<T, S extends DelegatedValueSH<T, S>> extends ValueSyncHandler<T, S> {

    protected T cache;

    protected final StoragePanel panel;
    protected final int slotIndex;

    protected DelegatedValueSH(StoragePanel panel, int slotIndex) {
        this.panel = panel;
        this.slotIndex = slotIndex;
    }

    public IStorageWrapper getWrapper() {
        return this.getPanel()
            .getWrapper();
    }

    public StoragePanel getPanel() {
        return panel;
    }

    public IUpgradeWrapper getUpgradeWrapper() {
        return this.getWrapper()
            .getUpgradeHandler()
            .getSlotWrapper(this.getSlotIndex());
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public abstract void write(PacketBuffer buffer) throws IOException;

    public abstract void read(PacketBuffer buffer) throws IOException;

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (id == SYNC_VALUE) {
            super.readOnClient(id, buf);
            return;
        }
        StorageSHRegistry.handleClient(this, id, buf);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (id == SYNC_VALUE) {
            super.readOnServer(id, buf);
            return;
        }
        StorageSHRegistry.handleServer(this, id, buf);
    }

    public T getValue() {
        return cache;
    }

    public void setValueInternal(T value, boolean sync) {
        this.cache = value;
        this.onValueChanged();
        if (sync) sync();
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
