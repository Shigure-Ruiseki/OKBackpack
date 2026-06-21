package ruiseki.okbackpack.compat.findit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
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

public class BackpackProvider implements IStackFilterProvider {

    @Override
    public IStackFilter getFilter(EntityPlayer player, TileEntity tileEntity) {
        if (!(tileEntity instanceof TEBackpack backpack)) return null;
        if (backpack.getWrapper() == null) return null;
        return getFilter(player, backpack.getWrapper());
    }

    @Override
    public IStackFilter getFilter(EntityPlayer player, ItemStack stack) {
        final Item item = stack.getItem();

        if (item instanceof BlockBackpack.ItemBackpack) {
            BackpackWrapper wrapper = BackpackEntityHelpers.getInteractionWrapper(player, stack);

            if (wrapper != null) {
                return getFilter(player, wrapper);
            }
        }

        return null;
    }

    public IStackFilter getFilter(EntityPlayer player, BackpackWrapper wrapper) {
        final AnyMultiItemFilter filter = new AnyMultiItemFilter();
        final FluidStackFilter fluidFilter = new FluidStackFilter();

        filter.add(new InventoryStackFilter(player, new BackpackWrapperInventoryAdapter(wrapper)));
        for (ITankUpgrade tank : wrapper.gatherCapabilityUpgrades(ITankUpgrade.class)
            .values()) {
            if (tank != null) {
                FluidStack fluid = tank.getContents();
                if (fluid != null && fluid.amount > 0) {
                    fluidFilter.add(fluid);
                }
            }
        }

        if (!fluidFilter.isEmpty()) {
            filter.add(fluidFilter);
        }

        return filter;
    }
}
