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
    public void build(UpgradeSlotUpdate group) {
        int slotIndex = group.getUpgradeIndex();

        String slotGroupName = "tank_slots_" + slotIndex;
        String slotSyncName = "tank_slot_" + slotIndex;

        // Inventory handler
        DelegatedItemHandlerSH tankInvHandler = new DelegatedItemHandlerSH(
            group.getPanel(),
            group.getPanel()::getContainer,
            slotIndex,
            4);
        group.getSyncManager()
            .syncValue("tank_delegation_" + slotIndex, tankInvHandler);
        group.put("tank_handler", tankInvHandler);

        // Input slot
        IndexedModularSlot inputSlot = new IndexedModularSlot(
            tankInvHandler.getItemHandler(),
            0,
            group.getPanel(),
            slotIndex);
        inputSlot.slotGroup(slotGroupName);
        group.getSyncManager()
            .syncValue(slotSyncName, 0, new ItemSlotSH(inputSlot));
        group.put("tank_input", inputSlot);

        // Output slot
        IndexedModularSlot outputSlot = new IndexedModularSlot(
            tankInvHandler.getItemHandler(),
            1,
            group.getPanel(),
            slotIndex);
        outputSlot.slotGroup(slotGroupName);
        group.getSyncManager()
            .syncValue(slotSyncName, 1, new ItemSlotSH(outputSlot));
        group.put("tank_output", outputSlot);

        group.getSyncManager()
            .registerSlotGroup(new SlotGroup(slotGroupName, 4, false));

        // Fluid stack
        DelegatedFluidStackSH fluidStackHandler = new DelegatedFluidStackSH(group.getPanel(), slotIndex);
        group.getSyncManager()
            .syncValue("tank_fluid_" + slotIndex, fluidStackHandler);
        group.put("tank_fluid", fluidStackHandler);

        // Tank capacity
        DelegatedIntSH tankCapacityHandler = new DelegatedIntSH(group.getPanel(), slotIndex);
        group.getSyncManager()
            .syncValue("tank_capacity_" + slotIndex, tankCapacityHandler);
        group.put("tank_capacity", tankCapacityHandler);
    }
}
