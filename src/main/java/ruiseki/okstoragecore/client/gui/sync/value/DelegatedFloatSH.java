package ruiseki.okstoragecore.client.gui.sync.value;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IDoubleSyncValue;
import com.cleanroommc.modularui.api.value.sync.IFloatSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.utils.FloatConsumer;
import com.cleanroommc.modularui.utils.FloatSupplier;

import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class DelegatedFloatSH extends DelegatedValueSH<Float, DelegatedFloatSH>
    implements IFloatSyncValue<Float>, IDoubleSyncValue<Float>, IStringSyncValue<Float> {

    public DelegatedFloat delegatedFloat;
    public float self = 0f;

    public DelegatedFloatSH(StoragePanel panel, int slotIndex) {
        super(panel, slotIndex);
        this.delegatedFloat = new DelegatedFloat(() -> this.self, value -> this.self = value);
        this.cache = 0f;
    }

    public void setDelegated(FloatSupplier getter) {
        this.setDelegated(getter, value -> {});
    }

    public void setDelegated(FloatSupplier getter, FloatConsumer setter) {
        this.delegatedFloat.setDelegated(getter, setter);
    }

    public void removeDelegated() {
        this.setDelegated(() -> this.self, value -> this.self = value);
        this.setValue(0f);
    }

    @Override
    public void setValue(Float value, boolean setSource, boolean sync) {
        setFloatValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        float value = this.delegatedFloat.getAsFloat();

        if (isFirstSync || Float.compare(value, cache) != 0) {
            setFloatValue(value, false, false);
            return true;
        }

        return false;
    }

    @Override
    public void notifyUpdate() {
        setFloatValue(this.delegatedFloat.getAsFloat(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeFloat(getFloatValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setFloatValue(buffer.readFloat(), true, false);
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        setFloatValue(Float.parseFloat(value), setSource, sync);
    }

    @Override
    public String getStringValue() {
        return String.valueOf(cache);
    }

    @Override
    public double getDoubleValue() {
        return getFloatValue();
    }

    @Override
    public void setDoubleValue(double value, boolean setSource, boolean sync) {
        setFloatValue((float) value, setSource, sync);
    }

    @Override
    public Class<Float> getValueType() {
        return Float.class;
    }

    @Override
    public Float getValue() {
        return cache;
    }

    @Override
    public float getFloatValue() {
        return cache;
    }

    @Override
    public void setFloatValue(float value, boolean setSource, boolean sync) {
        if (setSource) {
            delegatedFloat.accept(value);
        }

        setValueInternal(value, sync);
    }
}
