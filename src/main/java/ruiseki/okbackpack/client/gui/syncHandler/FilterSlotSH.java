package ruiseki.okbackpack.client.gui.syncHandler;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.PhantomItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

public class FilterSlotSH extends PhantomItemSlotSH {

    public FilterSlotSH(ModularSlot slot) {
        super(slot);
    }

    @Override
    protected void phantomClick(MouseData mouseData, ItemStack cursorStack) {
        if (mouseData.shift) {
            getSlot().putStack(null);
            return;
        }

        if (cursorStack != null) {
            if (!isItemValid(cursorStack)) return;

            ItemStack copy = cursorStack.copy();
            copy.stackSize = 1;
            getSlot().putStack(copy);
            return;
        }

        getSlot().putStack(null);
    }

    @Override
    protected void phantomScroll(MouseData mouseData) {

    }

}
