package ruiseki.okbackpack.client.gui.syncHandler;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

public class OreDictFilterSlotSH extends FilterSlotSH {

    public OreDictFilterSlotSH(ModularSlot slot) {
        super(slot);
    }

    @Override
    protected void phantomClick(MouseData mouseData, ItemStack cursorStack) {
        if (cursorStack != null && OreDictionary.getOreIDs(cursorStack).length == 0) {
            return;
        }
        super.phantomClick(mouseData, cursorStack);
    }
}
