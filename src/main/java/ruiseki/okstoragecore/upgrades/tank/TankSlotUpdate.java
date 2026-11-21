package ruiseki.okstoragecore.upgrades.tank;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedFluidStackSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedIntSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class TankSlotUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate slotUpdate) {
        int slotIndex = slotUpdate.getUpgradeIndex();

        String slotGroupName = "tank_slots_" + slotIndex;
        String slotSyncName = "tank_slot_" + slotIndex;

        // Inventory handler
        DelegatedItemHandlerSH tankInvHandler = new DelegatedItemHandlerSH(
            slotUpdate.getPanel(),
            slotUpdate.getPanel()::getContainer,
            slotIndex,
            4);
        slotUpdate.getSyncManager()
            .syncValue("tank_delegation_" + slotIndex, tankInvHandler);
        slotUpdate.put("tank_handler", tankInvHandler);

        // Input slot
        IndexedModularSlot inputSlot = new IndexedModularSlot(
            tankInvHandler.getItemHandler(),
            0,
            slotUpdate.getPanel(),
            slotIndex);
        inputSlot.slotGroup(slotGroupName);
        slotUpdate.getSyncManager()
            .syncValue(slotSyncName, 0, new ItemSlotSH(inputSlot));
        slotUpdate.put("tank_input", inputSlot);

        // Output slot
        IndexedModularSlot outputSlot = new IndexedModularSlot(
            tankInvHandler.getItemHandler(),
            1,
            slotUpdate.getPanel(),
            slotIndex);
        outputSlot.slotGroup(slotGroupName);
        slotUpdate.getSyncManager()
            .syncValue(slotSyncName, 1, new ItemSlotSH(outputSlot));
        slotUpdate.put("tank_output", outputSlot);

        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup(slotGroupName, 4, false));

        // Fluid stack
        DelegatedFluidStackSH fluidStackHandler = new DelegatedFluidStackSH(slotUpdate.getPanel(), slotIndex);
        slotUpdate.getSyncManager()
            .syncValue("tank_fluid_" + slotIndex, fluidStackHandler);
        slotUpdate.put("tank_fluid", fluidStackHandler);

        // Tank capacity
        DelegatedIntSH tankCapacityHandler = new DelegatedIntSH(slotUpdate.getPanel(), slotIndex);
        slotUpdate.getSyncManager()
            .syncValue("tank_capacity_" + slotIndex, tankCapacityHandler);
        slotUpdate.put("tank_capacity", tankCapacityHandler);
    }
}
