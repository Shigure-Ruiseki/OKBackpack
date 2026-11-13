package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IDoubleSyncValue;
import com.cleanroommc.modularui.api.value.sync.IFloatSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;

import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class DelegatedDoubleSH extends DelegatedValueSH<Double, DelegatedDoubleSH>
    implements IFloatSyncValue<Double>, IDoubleSyncValue<Double>, IStringSyncValue<Double> {

    public DelegatedDouble delegatedDouble;
    public double self = 0d;

    public DelegatedDoubleSH(StoragePanel panel, int slotIndex) {
        super(panel, slotIndex);
        this.delegatedDouble = new DelegatedDouble(() -> this.self, value -> this.self = value);
        this.cache = 0d;
    }

    public void setDelegated(DoubleSupplier getter) {
        this.setDelegated(getter, value -> {});
    }

    public void setDelegated(DoubleSupplier getter, DoubleConsumer setter) {
        this.delegatedDouble.setDelegated(getter, setter);
    }

    public void removeDelegated() {
        this.setDelegated(() -> this.self, value -> this.self = value);
        this.setValue(0d);
    }

    @Override
    public void setValue(Double value, boolean setSource, boolean sync) {
        setDoubleValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        double value = this.delegatedDouble.getAsDouble();

        if (isFirstSync || Double.compare(value, cache) != 0) {
            setDoubleValue(value, false, false);
            return true;
        }

        return false;
    }

    @Override
    public void notifyUpdate() {
        setDoubleValue(this.delegatedDouble.getAsDouble(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeDouble(getDoubleValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setDoubleValue(buffer.readDouble(), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setDoubleValue(Double.parseDouble(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(cache);
    }

    @Override
    public double getDoubleValue() {
        return cache;
    }

    @Override
    public void setDoubleValue(double value, boolean setSource, boolean sync) {
        if (setSource) {
            delegatedDouble.accept(value);
        }

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
