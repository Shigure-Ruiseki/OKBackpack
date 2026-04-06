package ruiseki.okbackpack.client.gui.syncHandler.value;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IBoolSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;

import ruiseki.okbackpack.api.IStorageWrapper;

public class DelegatedBooleanSH extends DelegatedValueSH<Boolean>
    implements IBoolSyncValue<Boolean>, IStringSyncValue<Boolean> {

    public DelegatedBooleanSupplier delegatedSupplier;

    public DelegatedBooleanSH(IStorageWrapper wrapper, int slotIndex) {
        super(wrapper, slotIndex);
        this.delegatedSupplier = new DelegatedBooleanSupplier(() -> false);
    }

    public void setDelegatedSupplier(DelegatedBooleanSupplier delegated) {
        delegatedSupplier.setDelegated(delegated);
    }

    @Override
    public void setValue(Boolean value, boolean setSource, boolean sync) {
        setBoolValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.delegatedSupplier.getAsBoolean() != this.cache) {
            setBoolValue(this.delegatedSupplier.getAsBoolean(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setBoolValue(this.delegatedSupplier.getAsBoolean(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeBoolean(this.delegatedSupplier.getAsBoolean());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setBoolValue(buffer.readBoolean(), buffer.readBoolean(), false);
    }

    @Override
    public void setBoolValue(boolean value, boolean setSource, boolean sync) {
        setValueInternal(value, sync);
    }

    @Override
    public boolean getBoolValue() {
        return cache;
    }

    @Override
    public String getStringValue() {
        return String.valueOf(cache);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setBoolValue(Boolean.getBoolean(value), setSource, sync);
    }

    @Override
    public Class<Boolean> getValueType() {
        return Boolean.class;
    }
}
