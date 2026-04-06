package ruiseki.okbackpack.client.gui.syncHandler.value;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IDoubleSyncValue;
import com.cleanroommc.modularui.api.value.sync.IFloatSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.utils.FloatSupplier;

import ruiseki.okbackpack.api.IStorageWrapper;

public class DelegatedFloatSH extends DelegatedValueSH<Float>
    implements IFloatSyncValue<Float>, IDoubleSyncValue<Float>, IStringSyncValue<Float> {

    public DelegatedFloatSupplier delegatedSupplier;

    public DelegatedFloatSH(IStorageWrapper wrapper, int slotIndex) {
        super(wrapper, slotIndex);
        this.delegatedSupplier = new DelegatedFloatSupplier(() -> 0f);
    }

    public void setDelegatedSupplier(FloatSupplier delegated) {
        delegatedSupplier.setDelegated(delegated);
    }

    @Override
    public void setValue(Float value, boolean setSource, boolean sync) {
        setFloatValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.delegatedSupplier.getAsFloat() != this.cache) {
            setFloatValue(this.delegatedSupplier.getAsFloat(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setFloatValue(this.delegatedSupplier.getAsFloat(), false, true);
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
        return String.valueOf(this.cache);
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
        setValueInternal(value, sync);
    }
}
