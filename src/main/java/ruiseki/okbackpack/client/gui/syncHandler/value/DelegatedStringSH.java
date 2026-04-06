package ruiseki.okbackpack.client.gui.syncHandler.value;

import java.io.IOException;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;

import ruiseki.okbackpack.api.IStorageWrapper;

public class DelegatedStringSH extends DelegatedValueSH<String> implements IStringSyncValue<String> {

    public DelegatedStringSupplier delegatedSupplier;

    public DelegatedStringSH(IStorageWrapper wrapper, int slotIndex) {
        super(wrapper, slotIndex);
        this.delegatedSupplier = new DelegatedStringSupplier(() -> "");
    }

    public void setDelegatedSupplier(Supplier<String> delegated) {
        delegatedSupplier.setDelegated(delegated);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.delegatedSupplier.get() != this.cache) {
            setStringValue(this.delegatedSupplier.get(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setStringValue(this.delegatedSupplier.get(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeStringToBuffer(getStringValue());
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        setStringValue(buffer.readStringFromBuffer(100), true, false);
    }

    @Override
    public void setValue(String value, boolean setSource, boolean sync) {
        setValueInternal(value, sync);
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setValueInternal(value, sync);
    }

    @Override
    public String getStringValue() {
        return cache;
    }
}
