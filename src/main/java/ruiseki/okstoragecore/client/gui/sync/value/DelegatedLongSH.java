package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import com.cleanroommc.modularui.api.value.sync.ILongSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;

import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class DelegatedLongSH extends DelegatedValueSH<Long, DelegatedLongSH>
    implements ILongSyncValue<Long>, IIntSyncValue<Long>, IStringSyncValue<Long> {

    public DelegatedLong delegatedLong;
    public long self = 0L;

    public DelegatedLongSH(StoragePanel panel, int slotIndex) {
        super(panel, slotIndex);
        this.delegatedLong = new DelegatedLong(() -> this.self, l -> this.self = l);
        this.cache = 0L;
    }

    public void setDelegated(LongSupplier getter) {
        this.setDelegated(getter, l -> {});
    }

    public void setDelegated(LongSupplier getter, LongConsumer setter) {
        this.delegatedLong.setDelegated(getter, setter);
    }

    public void removeDelegated() {
        this.setDelegated(() -> this.self, value -> this.self = value);
        this.setValue(0L);
    }

    @Override
    public void setValue(Long value, boolean setSource, boolean sync) {
        setLongValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        long value = this.delegatedLong.getAsLong();
        if (isFirstSync || value != cache) {
            setLongValue(value, false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setLongValue(this.delegatedLong.getAsLong(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeLong(getLongValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setLongValue(buffer.readLong(), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setLongValue(Long.parseLong(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(cache);
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
    public int getIntValue() {
        return Math.toIntExact(cache);
    }

    @Override
    public void setIntValue(int value, boolean setSource, boolean sync) {
        setLongValue(value, setSource, sync);
    }

    @Override
    public void setLongValue(long value, boolean setSource, boolean sync) {
        if (setSource) {
            this.delegatedLong.accept(value);
        }

        setValueInternal(value, sync);
    }

    @Override
    public long getLongValue() {
        return cache;
    }
}
