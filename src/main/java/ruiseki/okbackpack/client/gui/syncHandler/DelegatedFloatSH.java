package ruiseki.okbackpack.client.gui.syncHandler;

import java.io.IOException;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IDoubleSyncValue;
import com.cleanroommc.modularui.api.value.sync.IFloatSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.utils.FloatSupplier;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IProgressable;
import ruiseki.okbackpack.api.wrapper.ISmeltingUpgrade;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.handler.DelegatedFloatSupplier;

public class DelegatedFloatSH extends ValueSyncHandler<Float>
    implements IFloatSyncValue<Float>, IDoubleSyncValue<Float>, IStringSyncValue<Float> {

    public static final int UPDATE_PROGRESS = 1;
    public static final int UPDATE_FUEL = 2;

    public final IStorageWrapper wrapper;
    public final int slotIndex;

    public DelegatedFloatSupplier delegatedFloatSupplier;

    private float cache;

    public DelegatedFloatSH(IStorageWrapper wrapper, int slotIndex) {
        this.wrapper = wrapper;
        this.slotIndex = slotIndex;

        this.delegatedFloatSupplier = new DelegatedFloatSupplier(() -> () -> 0f);
    }

    public void setDelegatedSupplier(Supplier<FloatSupplier> delegated) {
        delegatedFloatSupplier.setDelegated(delegated);
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) throws IOException {
        super.readOnClient(id, buf);
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) throws IOException {

        IUpgradeWrapper wrapper = this.wrapper.getUpgradeHandler()
            .getWrapperInSlot(slotIndex);

        switch (id) {

            case UPDATE_PROGRESS:
                if (wrapper instanceof IProgressable upgrade) {
                    setDelegatedSupplier(() -> upgrade::getProgress);
                }
                break;

            case UPDATE_FUEL:
                if (wrapper instanceof ISmeltingUpgrade upgrade) {
                    setDelegatedSupplier(() -> upgrade::getBurnProgress);
                }
                break;

            default:
                super.readOnServer(id, buf);
        }
    }

    @Override
    public void setValue(Float value, boolean setSource, boolean sync) {
        setFloatValue(value, setSource, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || this.delegatedFloatSupplier.getAsFloat() != this.cache) {
            setFloatValue(this.delegatedFloatSupplier.getAsFloat(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        setFloatValue(this.delegatedFloatSupplier.getAsFloat(), false, true);
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
    public void setFloatValue(float value, boolean setSource, boolean sync) {
        this.cache = value;
        onValueChanged();
        if (sync) sync();
    }

    @Override
    public float getFloatValue() {
        return cache;
    }
}
