package ruiseki.okbackpack.client.gui.syncHandler.value;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.value.sync.ValueSyncHandler;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.DelegatedValueSHRegistry;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;

public abstract class DelegatedValueSH<T> extends ValueSyncHandler<T> {

    protected T cache;

    public final IStorageWrapper wrapper;
    public final int slotIndex;

    protected DelegatedValueSH(IStorageWrapper wrapper, int slotIndex) {
        this.wrapper = wrapper;
        this.slotIndex = slotIndex;
    }

    public abstract void write(PacketBuffer buffer) throws IOException;

    public abstract void read(PacketBuffer buffer) throws IOException;

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        if (!DelegatedValueSHRegistry.isClientEmpty()) {
            try {
                DelegatedValueSHRegistry.handleClient(this, id, buf);
                this.wrapper.markDirty();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        super.readOnClient(id, buf);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {
        if (!DelegatedValueSHRegistry.isServerEmpty()) {
            try {
                DelegatedValueSHRegistry.handleServer(this, id, buf);
                this.wrapper.markDirty();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        super.readOnServer(id, buf);
    }

    public T getValue() {
        return cache;
    }

    public void setValueInternal(T value, boolean sync) {
        this.cache = value;
        onValueChanged();
        if (sync) sync();
    }

    public IUpgradeWrapper getWrapper() {
        return this.wrapper.getUpgradeHandler()
            .getWrapperInSlot(slotIndex);
    }

    public static int getId(String name) {
        return DelegatedValueSHRegistry.getId(name);
    }
}
