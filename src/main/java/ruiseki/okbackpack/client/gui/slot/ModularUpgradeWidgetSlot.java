package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

public class ModularUpgradeWidgetSlot extends ModularSlot {

    public final int upgradeSlotIndex;

    public ModularUpgradeWidgetSlot(int upgradeSlotIndex, IItemHandler itemHandler, int index) {
        super(itemHandler, index);
        this.upgradeSlotIndex = upgradeSlotIndex;
    }

    public int getUpgradeSlotIndex() {
        return upgradeSlotIndex;
    }

    public boolean canShiftClickInsert(ItemStack stack) {
        return true;
    }
}
