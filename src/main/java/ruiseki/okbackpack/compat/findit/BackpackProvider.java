package ruiseki.okbackpack.compat.findit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;

import com.gtnh.findit.IStackFilter;
import com.gtnh.findit.IStackFilter.AnyMultiItemFilter;
import com.gtnh.findit.IStackFilter.FluidStackFilter;
import com.gtnh.findit.IStackFilter.IStackFilterProvider;
import com.gtnh.findit.IStackFilter.InventoryStackFilter;

import ruiseki.okbackpack.api.wrapper.ITankUpgrade;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.block.TEBackpack;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;
import ruiseki.okbackpack.common.inventory.BackpackWrapperInventoryAdapter;
import ruiseki.okcore.helper.ItemHelpers;

public class BackpackProvider implements IStackFilterProvider {

    @Override
    public IStackFilter getFilter(EntityPlayer player, TileEntity tileEntity) {
        if (!(tileEntity instanceof TEBackpack backpack)) return null;
        BackpackWrapper wrapper = backpack.getWrapper();
        return wrapper != null ? getFilter(player, wrapper) : null;
    }

    @Override
    public IStackFilter getFilter(EntityPlayer player, ItemStack stack) {
        return ItemHelpers.getItem(stack, BlockBackpack.ItemBackpack.class)
            .map(item -> BackpackEntityHelpers.getInteractionWrapper(player, stack))
            .map(wrapper -> getFilter(player, wrapper))
            .orElse(null);
    }

    public IStackFilter getFilter(EntityPlayer player, BackpackWrapper wrapper) {
        AnyMultiItemFilter filter = new AnyMultiItemFilter();
        FluidStackFilter fluidFilter = new FluidStackFilter();
        filter.add(new InventoryStackFilter(player, new BackpackWrapperInventoryAdapter(wrapper)));
        for (ITankUpgrade tank : wrapper.gatherCapabilityUpgrades(ITankUpgrade.class)
            .values()) {
            if (tank == null) continue;
            FluidStack fluid = tank.getContents();
            if (fluid != null && fluid.amount > 0) {
                fluidFilter.add(fluid);
            }
        }
        if (!fluidFilter.isEmpty()) {
            filter.add(fluidFilter);
        }
        return filter;
    }
}
