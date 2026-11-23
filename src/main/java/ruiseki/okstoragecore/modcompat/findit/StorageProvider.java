package ruiseki.okstoragecore.modcompat.findit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;

import com.gtnh.findit.IStackFilter;
import com.gtnh.findit.IStackFilter.AnyMultiItemFilter;
import com.gtnh.findit.IStackFilter.FluidStackFilter;
import com.gtnh.findit.IStackFilter.InventoryStackFilter;

import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.FluidHelpers;
import ruiseki.okcore.item.capability.wrapper.InventoryAdapter;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.capabilities.CapabilityStorageWrapperConfig;

public class StorageProvider implements IStackFilter.IStackFilterProvider {

    @Override
    public IStackFilter getFilter(EntityPlayer player, TileEntity tileEntity) {
        return CapabilityHelpers.getCapability(tileEntity, CapabilityStorageWrapperConfig.CAPABILITY)
            .map(wrapper -> getFilter(player, wrapper))
            .orElse(null);
    }

    @Override
    public IStackFilter getFilter(EntityPlayer player, ItemStack stack) {
        return CapabilityHelpers.getCapability(stack, CapabilityStorageWrapperConfig.CAPABILITY)
            .map(wrapper -> getFilter(player, wrapper))
            .orElse(null);
    }

    public IStackFilter getFilter(EntityPlayer player, IStorageWrapper wrapper) {
        AnyMultiItemFilter filter = new AnyMultiItemFilter();
        FluidStackFilter fluidFilter = new FluidStackFilter();
        filter.add(new InventoryStackFilter(player, new InventoryAdapter(wrapper.getInventoryHandler())));
        wrapper.getFluidHandler()
            .ifPresent(handler -> {
                for (int i = 0; i < handler.getTanks(); i++) {
                    FluidStack fluid = handler.getFluidInTank(i);
                    if (FluidHelpers.isEmpty(fluid)) {
                        fluidFilter.add(fluid);
                    }
                }
            });
        if (!fluidFilter.isEmpty()) {
            filter.add(fluidFilter);
        }
        return filter;
    }
}
