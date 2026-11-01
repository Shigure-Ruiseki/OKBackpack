package ruiseki.okstoragecore.client.gui.sync.value;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import com.cleanroommc.modularui.api.value.sync.IStringSyncValue;
import com.cleanroommc.modularui.network.NetworkUtils;

import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class DelegatedFluidStackSH extends DelegatedValueSH<FluidStack, DelegatedFluidStackSH>
    implements IStringSyncValue<FluidStack> {

    public DelegatedFluidStack delegatedSupplier;

    public DelegatedFluidStackSH(StoragePanel panel, int slotIndex) {
        super(panel, slotIndex);
        this.delegatedSupplier = new DelegatedFluidStack(() -> FluidHelpers.EMPTY);
        this.cache = FluidHelpers.EMPTY;
    }

    public void setDelegatedSupplier(Supplier<FluidStack> delegated) {
        this.delegatedSupplier.setDelegated(delegated);
    }

    @Override
    public void setValue(FluidStack value, boolean setSource, boolean sync) {
        this.setValueInternal(FluidHelpers.isEmpty(value) ? FluidHelpers.EMPTY : FluidHelpers.copy(value), sync);
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (isFirstSync || !FluidHelpers.areFluidStacksEqual(this.delegatedSupplier.get(), cache)) {
            this.setValue(this.delegatedSupplier.get(), false, false);
            return true;
        }
        return false;
    }

    @Override
    public void notifyUpdate() {
        this.setValue(this.delegatedSupplier.get(), false, true);
    }

    @Override
    public void write(PacketBuffer buffer) {
        FluidStack value = this.delegatedSupplier.get();
        buffer.writeBoolean(!FluidHelpers.isEmpty(value));
        if (value != null) {
            NetworkUtils.writeFluidStack(buffer, value);
        }
    }

    @Override
    public void read(PacketBuffer buffer) {
        FluidStack value = buffer.readBoolean() ? NetworkUtils.readFluidStack(buffer) : FluidHelpers.EMPTY;
        this.setValue(value, false, false);
    }

    @Override
    public String getStringValue() {
        return FluidHelpers.isEmpty(this.cache) ? "" : this.cache.getLocalizedName();
    }

    @Override
    public void setStringValue(String value, boolean setSource, boolean sync) {
        // FluidStack is not safely reconstructible from its localized name.
        // Use setValue() to update the actual FluidStack.
    }

    @Override
    public Class<FluidStack> getValueType() {
        return FluidStack.class;
    }
}
