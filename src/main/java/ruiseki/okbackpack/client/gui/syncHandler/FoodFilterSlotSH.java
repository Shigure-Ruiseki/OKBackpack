package ruiseki.okbackpack.client.gui.syncHandler;

import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

public class FoodFilterSlotSH extends FilterSlotSH {

    public FoodFilterSlotSH(ModularSlot slot) {
        super(slot);
    }

    @Override
    public boolean isItemValid(ItemStack itemStack) {
        return itemStack.getItem() instanceof ItemFood;
    }
}
