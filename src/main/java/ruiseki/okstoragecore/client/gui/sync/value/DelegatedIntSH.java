package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IDoubleSyncValue;
import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;

import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class DelegatedIntSH extends DelegatedValueSH<Integer, DelegatedIntSH>
    implements IIntSyncValue<Integer>, IDoubleSyncValue<Integer>, IStringSyncValue<Integer> {

    public DelegatedInt delegatedInt;
    public int self = 0;

    public DelegatedIntSH(StoragePanel panel, int slotIndex) {
        super(panel, slotIndex);
        this.delegatedInt = new DelegatedInt(() -> this.self, i -> this.self = i);
        this.cache = 0;
    }

    public void setDelegated(IntSupplier getter) {
        this.setDelegated(getter, i -> {});
    }

    public void setDelegated(IntSupplier getter, IntConsumer setter) {
        this.delegatedInt.setDelegated(getter, setter);
    }

    public void removeDelegated() {
        this.setDelegated(() -> this.self, value -> this.self = value);
        this.setValue(0);
    }

    @Override
    public void setValue(Integer value, boolean setSource, boolean sync) {
        setIntValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        int value = this.delegatedInt.getAsInt();

        if (isFirstSync || value != cache) {
            setIntValue(value, false, false);
            return true;
        }

        return false;
    }

    @Override
    public void notifyUpdate() {
        setIntValue(this.delegatedInt.getAsInt(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeInt(getIntValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setIntValue(buffer.readInt(), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setIntValue(Integer.parseInt(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(cache);
    }

    @Override
    public double getDoubleValue() {
        return getIntValue();
    }

    @Override
    public void setDoubleValue(double value, boolean setSource, boolean sync) {
        setIntValue((int) value, setSource, sync);
    }

    @Override
    public Class<Integer> getValueType() {
        return Integer.class;
    }

    @Override
    public Integer getValue() {
        return cache;
    }

    @Override
    public void setIntValue(int value, boolean setSource, boolean sync) {
        if (setSource) {
            this.delegatedInt.accept(value);
        }

        setValueInternal(value, sync);
    }

    @Override
    public int getIntValue() {
        return cache;
    }
}
