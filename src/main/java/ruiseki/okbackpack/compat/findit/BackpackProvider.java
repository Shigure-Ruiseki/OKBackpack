package ruiseki.okbackpack.compat.findit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.gtnh.findit.IStackFilter;
import com.gtnh.findit.IStackFilter.IStackFilterProvider;
import com.gtnh.findit.IStackFilter.InventoryStackFilter;

import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelpers;

public class BackpackProvider implements IStackFilterProvider {

    @Override
    public IStackFilter getFilter(EntityPlayer player, TileEntity tileEntity) {
        return null;
    }

    @Override
    public IStackFilter getFilter(EntityPlayer player, ItemStack stack) {
        final Item item = stack.getItem();

        if (item instanceof BlockBackpack.ItemBackpack) {
            BackpackWrapper wrapper = BackpackEntityHelpers.getInteractionWrapper(player, stack);

            if (wrapper != null) {
                return new InventoryStackFilter(player, wrapper);
            }
        }

        return null;
    }
}
