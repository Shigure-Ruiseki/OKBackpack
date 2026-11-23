package ruiseki.okstoragecore.upgrades.battery;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedIntSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class BatterySlotUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate slotUpdate) {
        int slotIndex = slotUpdate.getUpgradeIndex();

        DelegatedItemHandlerSH batteryHandler = new DelegatedItemHandlerSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotIndex,
            2);

        slotUpdate.getSyncManager()
            .syncValue("battery_delegation_" + slotIndex, batteryHandler);
        slotUpdate.put("battery_handler", batteryHandler);

        // Input slot (index 0) - items that provide energy
        IndexedModularSlot inputSlot = new IndexedModularSlot(
            batteryHandler.getItemHandler(),
            0,
            slotUpdate.getPanel(),
            slotIndex);
        inputSlot.slotGroup("battery_slots_" + slotIndex);
        slotUpdate.getSyncManager()
            .syncValue("battery_slot_" + slotIndex, 0, new ItemSlotSH(inputSlot));
        slotUpdate.put("battery_input", inputSlot);

        // Output slot (index 1) - items that receive energy
        IndexedModularSlot outputSlot = new IndexedModularSlot(
            batteryHandler.getItemHandler(),
            1,
            slotUpdate.getPanel(),
            slotIndex);
        outputSlot.slotGroup("battery_slots_" + slotIndex);
        slotUpdate.getSyncManager()
            .syncValue("battery_slot_" + slotIndex, 1, new ItemSlotSH(outputSlot));
        slotUpdate.put("battery_output", outputSlot);

        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup("battery_slots_" + slotIndex, 2, false));

        // Energy stored value sync
        DelegatedIntSH energyStoredHandler = new DelegatedIntSH(slotUpdate.getPanel(), slotIndex);
        slotUpdate.getSyncManager()
            .syncValue("battery_energy_stored_" + slotIndex, energyStoredHandler);
        slotUpdate.put("battery_energy_stored", energyStoredHandler);

        // Max energy value sync
        DelegatedIntSH maxEnergyHandler = new DelegatedIntSH(slotUpdate.getPanel(), slotIndex);
        slotUpdate.getSyncManager()
            .syncValue("battery_max_energy_" + slotIndex, maxEnergyHandler);
        slotUpdate.put("battery_max_energy", maxEnergyHandler);
    }
}
