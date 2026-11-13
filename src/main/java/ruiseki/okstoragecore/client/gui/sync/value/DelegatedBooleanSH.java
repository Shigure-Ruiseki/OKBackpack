package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.BooleanSupplier;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IBoolSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.utils.BooleanConsumer;

import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class DelegatedBooleanSH extends DelegatedValueSH<Boolean, DelegatedBooleanSH>
    implements IBoolSyncValue<Boolean>, IStringSyncValue<Boolean> {

    public DelegatedBoolean delegatedBoolean;
    public boolean self = false;

    public DelegatedBooleanSH(StoragePanel panel, int slotIndex) {
        super(panel, slotIndex);
        this.delegatedBoolean = new DelegatedBoolean(() -> this.self, value -> this.self = value);
        this.cache = false;
    }

    public void setDelegated(BooleanSupplier getter) {
        this.setDelegated(getter, value -> {});
    }

    public void setDelegated(BooleanSupplier getter, BooleanConsumer setter) {
        this.delegatedBoolean.setDelegated(getter, setter);
    }

    public void removeDelegated() {
        this.setDelegated(() -> this.self, value -> this.self = value);
        this.setValue(false);
    }

    @Override
    public void setValue(Boolean value, boolean setSource, boolean sync) {
        setBoolValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        boolean value = this.delegatedBoolean.getAsBoolean();

        if (isFirstSync || value != cache) {
            setBoolValue(value, false, false);
            return true;
        }

        return false;
    }

    @Override
    public void notifyUpdate() {
        setBoolValue(this.delegatedBoolean.getAsBoolean(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeBoolean(getBoolValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setBoolValue(buffer.readBoolean(), true, false);
    }

    @Override
    public void setBoolValue(boolean value, boolean setSource, boolean sync) {
        if (setSource) {
            delegatedBoolean.accept(value);
        }

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
        setBoolValue(Boolean.parseBoolean(value), setSource, sync);
    }

    @Override
    public Class<Boolean> getValueType() {
        return Boolean.class;
    }
}
