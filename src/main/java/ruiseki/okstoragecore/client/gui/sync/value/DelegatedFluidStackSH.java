package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.network.NetworkUtils;

import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class DelegatedFluidStackSH extends DelegatedValueSH<FluidStack, DelegatedFluidStackSH>
    implements IStringSyncValue<FluidStack> {

    public DelegatedFluidStack delegatedFluidStack;
    public FluidStack self = FluidHelpers.EMPTY;

    public DelegatedFluidStackSH(StoragePanel panel, int slotIndex) {
        super(panel, slotIndex);
        this.delegatedFluidStack = new DelegatedFluidStack(() -> this.self, value -> this.self = value);
        this.cache = FluidHelpers.EMPTY;
    }

    public void setDelegated(Supplier<FluidStack> getter) {
        this.setDelegated(getter, value -> {});
    }

    public void setDelegated(Supplier<FluidStack> getter, Consumer<FluidStack> setter) {
        this.delegatedFluidStack.setDelegated(getter, setter);
    }

    public void removeDelegated() {
        this.setDelegated(() -> this.self, value -> this.self = value);
        this.setValue(FluidHelpers.EMPTY);
    }

    @Override
    public void setValue(FluidStack value, boolean setSource, boolean sync) {
        FluidStack copied = FluidHelpers.isEmpty(value) ? FluidHelpers.EMPTY : FluidHelpers.copy(value);

        if (setSource) {
            this.delegatedFluidStack
                .accept(FluidHelpers.isEmpty(copied) ? FluidHelpers.EMPTY : FluidHelpers.copy(copied));
        }

        this.setValueInternal(copied, sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        FluidStack value = this.delegatedFluidStack.get();

        if (isFirstSync || !FluidHelpers.areFluidStacksEqual(value, cache)) {
            this.setValue(value, false, false);
            return true;
        }

        return false;
    }

    @Override
    public void notifyUpdate() {
        this.setValue(this.delegatedFluidStack.get(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) {
        FluidStack value = cache;

        buffer.writeBoolean(!FluidHelpers.isEmpty(value));

        if (!FluidHelpers.isEmpty(value)) {
            NetworkUtils.writeFluidStack(buffer, value);
        }
    }

    @Override
    public void read(PacketBuffer buffer) {
        FluidStack value = buffer.readBoolean() ? NetworkUtils.readFluidStack(buffer) : FluidHelpers.EMPTY;

        this.setValue(value, true, false);
    }

    @Override
    public String getStringValue() {
        return FluidHelpers.isEmpty(cache) ? "" : cache.getLocalizedName();
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        // FluidStack cannot be safely reconstructed from its localized name.
    }

    @Override
    public Class<FluidStack> getValueType() {
        return FluidStack.class;
    }
}
