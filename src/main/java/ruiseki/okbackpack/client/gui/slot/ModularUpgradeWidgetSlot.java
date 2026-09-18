package ruiseki.okbackpack.client.gui.slot;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.client.mui.gui.component.slot.ModularItemSlot;
import ruiseki.okcore.item.handler.IItemHandler;

public class ModularUpgradeWidgetSlot extends ModularItemSlot {

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
