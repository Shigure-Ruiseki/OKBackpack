package ruiseki.okbackpack.client.gui.slot;

import com.cleanroommc.modularui.widgets.slot.ModularCraftingSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

public class CraftingSlotInfo {

    private final ModularSlot[] craftingMatrixSlots;
    private final ModularCraftingSlot craftingOutputSlot;

    public CraftingSlotInfo(ModularSlot[] craftingMatrixSlots, ModularCraftingSlot craftingOutputSlot) {
        this.craftingMatrixSlots = craftingMatrixSlots;
        this.craftingOutputSlot = craftingOutputSlot;
    }

    public ModularSlot[] getCraftingMatrixSlots() {
        return craftingMatrixSlots;
    }

    public ModularCraftingSlot getCraftingOutputSlot() {
        return craftingOutputSlot;
    }
}
