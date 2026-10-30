package ruiseki.okstoragecore.upgrades.battery;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.client.gui.slot.CustomBackgroundSlot;
import ruiseki.okcore.client.mui.OKCGuiTextures;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class BatteryUpgradeTab extends ExpandedUpgradeTab<BatteryUpgradeWrapper> {

    public BatteryUpgradeTab(int slotIndex, BatteryUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
        super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.battery");
    }

    @Override
    protected Flow addUpgradeColumn() {
        return super.addUpgradeColumn().child(this.addSlotRow());
    }

    protected Flow addSlotRow() {

        // Input slot (items that provide energy to battery)
        ItemSlot inputSlot = new CustomBackgroundSlot(OKCGuiTextures.EMPTY_BATTERY_INPUT)
            .syncHandler("battery_slot_" + slotIndex, 0)
            .name("battery_input_" + slotIndex);

        // Output slot (items that receive energy from battery)
        ItemSlot outputSlot = new CustomBackgroundSlot(OKCGuiTextures.EMPTY_BATTERY_OUTPUT)
            .syncHandler("battery_slot_" + slotIndex, 1)
            .name("battery_output_" + slotIndex);

        return Flow.row()
            .coverChildren()
            .childPadding(2)
            .child(inputSlot)
            .child(outputSlot);
    }
}
