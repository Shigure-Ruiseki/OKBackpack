package ruiseki.okbackpack.client.gui.syncHandler;

import java.io.IOException;
import java.util.function.Supplier;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import com.cleanroommc.modularui.api.value.sync.IDoubleSyncValue;
import com.cleanroommc.modularui.api.value.sync.IFloatSyncValue;
import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.utils.FloatSupplier;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;

import ruiseki.okbackpack.api.wrapper.IProgressable;
import ruiseki.okbackpack.client.gui.handler.DelegatedFloatSupplier;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperBase;
import ruiseki.okbackpack.common.item.wrapper.UpgradeWrapperFactory;

public class DelegatedFloatSH extends ValueSyncHandler<Float>
    implements IFloatSyncValue<Float>, IDoubleSyncValue<Float>, IStringSyncValue<Float> {

    public static final int UPDATE_PROGRESS = 1;

    public final BackpackWrapper wrapper;
    public final int slotIndex;

    public DelegatedFloatSupplier delegatedFloatSupplier;

    private float cache;

    public DelegatedFloatSH(BackpackWrapper wrapper, int slotIndex) {
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

        ItemStack stack = wrapper.upgradeHandler.getStackInSlot(slotIndex);

        UpgradeWrapperBase upgradeWrapper = UpgradeWrapperFactory.createWrapper(stack, wrapper);

        switch (id) {

            case UPDATE_PROGRESS:
                if (upgradeWrapper instanceof IProgressable upgrade) {
                    setDelegatedSupplier(() -> upgrade::getProgress);
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
