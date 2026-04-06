package ruiseki.okbackpack.client.gui.syncHandler.value;

import java.util.function.LongSupplier;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import com.cleanroommc.modularui.api.value.sync.ILongSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;

import ruiseki.okbackpack.api.IStorageWrapper;

public class DelegatedLongSH extends DelegatedValueSH<Long>
    implements ILongSyncValue<Long>, IIntSyncValue<Long>, IStringSyncValue<Long> {

    public DelegatedLongSupplier delegatedSupplier;

    public DelegatedLongSH(IStorageWrapper wrapper, int slotIndex) {
        super(wrapper, slotIndex);
        this.delegatedSupplier = new DelegatedLongSupplier(() -> 0);
    }

    public void setDelegatedSupplier(LongSupplier delegated) {
        delegatedSupplier.setDelegated(delegated);
    }

    @Override
    public void setValue(Long value, boolean setSource, boolean sync) {
        setLongValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.delegatedSupplier.getAsLong() != this.cache) {
            setLongValue(this.delegatedSupplier.getAsLong(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setLongValue(this.delegatedSupplier.getAsLong(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeFloat(getIntValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setLongValue(buffer.readInt(), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setLongValue(Integer.parseInt(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.cache);
    }

    @Override
    public Class<Long> getValueType() {
        return Long.class;
    }

    @Override
    public Long getValue() {
        return cache;
    }

    @Override
    public void setIntValue(int value, boolean setSource, boolean sync) {
        setLongValue(value, sync);
    }

    @Override
    public int getIntValue() {
        return Math.toIntExact(cache);
    }

    @Override
    public void setLongValue(long value, boolean setSource, boolean sync) {
        setValueInternal(value, sync);
    }

    @Override
    public long getLongValue() {
        return cache;
    }
}
