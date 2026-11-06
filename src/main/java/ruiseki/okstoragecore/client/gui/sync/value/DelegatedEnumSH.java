package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.IEnumValue;
import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import com.cleanroommc.modularui.network.NetworkUtils;

import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class DelegatedEnumSH<T extends Enum<T>> extends DelegatedValueSH<T, DelegatedEnumSH<T>>
    implements IEnumValue<T>, IIntSyncValue<T> {

    protected final Class<T> enumClass;
    public DelegatedEnum<T> delegatedEnum;
    public T self;

    public DelegatedEnumSH(StoragePanel panel, int slotIndex, Class<T> enumClass) {
        super(panel, slotIndex);
        this.enumClass = enumClass;
        this.self = enumClass.getEnumConstants()[0];
        this.cache = this.self;
        this.delegatedEnum = new DelegatedEnum<>(() -> this.self, value -> this.self = value);
    }

    public void setDelegated(Supplier<T> getter) {
        this.setDelegated(getter, value -> {});
    }

    public void setDelegated(Supplier<T> getter, Consumer<T> setter) {
        this.delegatedEnum.setDelegated(getter, setter);
    }

    public void removeDelegated() {
        this.setDelegated(() -> this.self, value -> this.self = value);
        this.setValue(this.enumClass.getEnumConstants()[0]);
    }

    @Override
    public void setValue(T value, boolean setSource, boolean sync) {
        if (setSource) {
            this.delegatedEnum.accept(value);
        }

        setValueInternal(value, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        T value = this.delegatedEnum.get();

        if (isFirstSync || value != cache) {
            setValue(value, false, false);
            return true;
        }

        return false;
    }

    @Override
    public void notifyUpdate() {
        setValue(this.delegatedEnum.get(), false, true);
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
        T[] constants = this.enumClass.getEnumConstants();

        if (value < 0 || value >= constants.length) {
            throw new IllegalArgumentException("Invalid enum ordinal: " + value);
        }

        setValue(constants[value], setSource, sync);
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
