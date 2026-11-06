package ruiseki.okstoragecore.client.gui.sync.value;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;

import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class DelegatedStringSH extends DelegatedValueSH<String, DelegatedStringSH> implements IStringSyncValue<String> {

    public DelegatedString delegatedString;
    public String self = "";

    public DelegatedStringSH(StoragePanel panel, int slotIndex) {
        super(panel, slotIndex);
        this.delegatedString = new DelegatedString(() -> this.self, value -> this.self = value);
        this.cache = "";
    }

    public void setDelegated(Supplier<String> getter) {
        this.setDelegated(getter, s -> {});
    }

    public void setDelegated(Supplier<String> getter, Consumer<String> setter) {
        this.delegatedString.setDelegated(getter, setter);
    }

    public void removeDelegated() {
        this.setDelegated(() -> this.self, value -> this.self = value);
        this.setValue("");
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || !Objects.equals(this.delegatedString.get(), this.cache)) {
            setStringValue(this.delegatedString.get(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setStringValue(this.delegatedString.get(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) throws IOException {
        buffer.writeStringToBuffer(getStringValue());
    }

    @Override
    public void read(PacketBuffer buffer) throws IOException {
        setStringValue(buffer.readStringFromBuffer(100), true, false);
    }

    @Override
    public void setValue(String value, boolean setSource, boolean sync) {
        if (setSource) {
            this.delegatedString.set(value);
        }
        setValueInternal(value, sync);
    }

    @Override
    public Class<String> getValueType() {
        return String.class;
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        this.setValue(value, setSource, sync);
    }

    @Override
    public String getStringValue() {
        return cache;
    }
}
