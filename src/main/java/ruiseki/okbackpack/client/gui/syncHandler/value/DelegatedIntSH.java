package ruiseki.okbackpack.client.gui.syncHandler.value;

import java.util.function.IntSupplier;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IDoubleSyncValue;
import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;

import ruiseki.okbackpack.api.IStorageWrapper;

public class DelegatedIntSH extends DelegatedValueSH<Integer>
    implements IIntSyncValue<Integer>, IDoubleSyncValue<Integer>, IStringSyncValue<Integer> {

    public DelegatedIntSupplier delegatedSupplier;

    public DelegatedIntSH(IStorageWrapper wrapper, int slotIndex) {
        super(wrapper, slotIndex);
        this.delegatedSupplier = new DelegatedIntSupplier(() -> 0);
    }

    public void setDelegatedSupplier(IntSupplier delegated) {
        delegatedSupplier.setDelegated(delegated);
    }

    @Override
    public void setValue(Integer value, boolean setSource, boolean sync) {
        setIntValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.delegatedSupplier.getAsInt() != this.cache) {
            setIntValue(this.delegatedSupplier.getAsInt(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setIntValue(this.delegatedSupplier.getAsInt(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeFloat(getIntValue());
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
        return String.valueOf(this.cache);
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
        setValueInternal(value, sync);
    }

    @Override
    public int getIntValue() {
        return cache;
    }
}
