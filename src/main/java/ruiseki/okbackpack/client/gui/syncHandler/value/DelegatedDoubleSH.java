package ruiseki.okbackpack.client.gui.syncHandler.value;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IDoubleSyncValue;
import com.cleanroommc.modularui.api.value.sync.IFloatSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;

import ruiseki.okbackpack.api.IStorageWrapper;

public class DelegatedDoubleSH extends DelegatedValueSH<Double>
    implements IFloatSyncValue<Double>, IDoubleSyncValue<Double>, IStringSyncValue<Double> {

    public DelegatedDoubleSupplier delegatedSupplier;

    public DelegatedDoubleSH(IStorageWrapper wrapper, int slotIndex) {
        super(wrapper, slotIndex);
        this.delegatedSupplier = new DelegatedDoubleSupplier(() -> 0);
    }

    public void setDelegatedSupplier(DelegatedDoubleSupplier delegated) {
        delegatedSupplier.setDelegated(delegated);
    }

    @Override
    public void setValue(Double value, boolean setSource, boolean sync) {
        setDoubleValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.delegatedSupplier.getAsDouble() != this.cache) {
            setDoubleValue(this.delegatedSupplier.getAsDouble(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setDoubleValue(this.delegatedSupplier.getAsDouble(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeDouble(getFloatValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setDoubleValue(buffer.readDouble(), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setDoubleValue(Float.parseFloat(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(this.cache);
    }

    @Override
    public double getDoubleValue() {
        return cache;
    }

    @Override
    public void setDoubleValue(double value, boolean setSource, boolean sync) {
        setValueInternal(value, sync);
    }

    @Override
    public Class<Double> getValueType() {
        return Double.class;
    }

    @Override
    public Double getValue() {
        return cache;
    }

    @Override
    public float getFloatValue() {
        return (float) getDoubleValue();
    }

    @Override
    public void setFloatValue(float value, boolean setSource, boolean sync) {
        setDoubleValue(value, setSource, sync);
    }
}
