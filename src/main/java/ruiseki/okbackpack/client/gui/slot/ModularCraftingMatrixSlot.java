package ruiseki.okbackpack.client.gui.slot;

import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import lombok.Getter;
import lombok.Setter;

public class ModularCraftingMatrixSlot extends ModularSlot {

    @Getter
    @Setter
    private boolean isActive = true;

    public ModularCraftingMatrixSlot(IItemHandler itemHandler, int index) {
        super(itemHandler, index);
    }
}
