package ruiseki.okbackpack.client.gui.syncHandler.value;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.IEnumValue;
import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import com.cleanroommc.modularui.network.NetworkUtils;

import ruiseki.okbackpack.api.IStorageWrapper;

public class DelegatedEnumSH<T extends Enum<T>> extends DelegatedValueSH<T> implements IEnumValue<T>, IIntSyncValue<T> {

    protected final Class<T> enumClass;
    public DelegatedEnumSupplier<T> delegatedSupplier;

    public DelegatedEnumSH(IStorageWrapper wrapper, int slotIndex, Class<T> enumClass) {
        super(wrapper, slotIndex);
        this.enumClass = enumClass;
        this.delegatedSupplier = new DelegatedEnumSupplier<T>(() -> null);
    }

    public void setDelegatedSupplier(Supplier<T> delegated) {
        delegatedSupplier.setDelegated(delegated);
    }

    @Override
    public void setValue(T value, boolean setSource, boolean sync) {
        setValueInternal(value, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.delegatedSupplier.get() != this.cache) {
            setValue(this.delegatedSupplier.get(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setValue(this.delegatedSupplier.get(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) {
        NetworkUtils.writeEnumValue(buffer, getValue());
    }

    @Override
    public void read(PacketBuffer buffer) {
        setValue(NetworkUtils.readEnumValue(buffer, this.enumClass), true, false);
    }

    @Override
    public Class<T> getEnumClass() {
        return this.enumClass;
    }

    @Override
    public void setIntValue(int value, boolean setSource, boolean sync) {
        setValue(this.enumClass.getEnumConstants()[value], setSource, sync);
    }

    @Override
    public int getIntValue() {
        return this.cache.ordinal();
    }

    @Override
    public Class<T> getValueType() {
        return this.enumClass;
    }
}
