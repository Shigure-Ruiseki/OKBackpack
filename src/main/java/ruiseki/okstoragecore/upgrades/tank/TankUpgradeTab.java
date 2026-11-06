
package ruiseki.okstoragecore.upgrades.tank;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.client.gui.slot.CustomBackgroundSlot;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class TankUpgradeTab extends ExpandedUpgradeTab<TankUpgradeWrapper> {

    public TankUpgradeTab(int slotIndex, TankUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
        super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.tank");
    }

    @Override
    protected Flow addUpgradeColumn() {
        return super.addUpgradeColumn().child(this.addSlotRow());
    }

    protected Flow addSlotRow() {
        ItemSlot inputSlot = new CustomBackgroundSlot(OSCTextures.EMPTY_TANK_INPUT)
            .syncHandler("tank_slot_" + slotIndex, 0)
            .name("tank_input_" + slotIndex);

        ItemSlot outputSlot = new CustomBackgroundSlot(OSCTextures.EMPTY_TANK_OUTPUT)
            .syncHandler("tank_slot_" + slotIndex, 1)
            .name("tank_output_" + slotIndex);

        return Flow.row()
            .coverChildren()
            .childPadding(2)
            .child(inputSlot)
            .child(outputSlot);
    }
}
