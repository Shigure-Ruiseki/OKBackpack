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
    public void build(UpgradeSlotUpdate group) {
        int slotIndex = group.getUpgradeIndex();

        DelegatedItemHandlerSH batteryHandler = new DelegatedItemHandlerSH(
            group.getPanel(),
            group::getContainer,
            slotIndex,
            2);

        group.getSyncManager()
            .syncValue("battery_delegation_" + slotIndex, batteryHandler);
        group.put("battery_handler", batteryHandler);

        // Input slot (index 0) - items that provide energy
        IndexedModularSlot inputSlot = new IndexedModularSlot(
            batteryHandler.getItemHandler(),
            0,
            group.getPanel(),
            slotIndex);
        inputSlot.slotGroup("battery_slots_" + slotIndex);
        group.getSyncManager()
            .syncValue("battery_slot_" + slotIndex, 0, new ItemSlotSH(inputSlot));
        group.put("battery_input", inputSlot);

        // Output slot (index 1) - items that receive energy
        IndexedModularSlot outputSlot = new IndexedModularSlot(
            batteryHandler.getItemHandler(),
            1,
            group.getPanel(),
            slotIndex);
        outputSlot.slotGroup("battery_slots_" + slotIndex);
        group.getSyncManager()
            .syncValue("battery_slot_" + slotIndex, 1, new ItemSlotSH(outputSlot));
        group.put("battery_output", outputSlot);

        group.getSyncManager()
            .registerSlotGroup(new SlotGroup("battery_slots_" + slotIndex, 2, false));

        // Energy stored value sync
        DelegatedIntSH energyStoredHandler = new DelegatedIntSH(group.getPanel(), slotIndex);
        group.getSyncManager()
            .syncValue("battery_energy_stored_" + slotIndex, energyStoredHandler);
        group.put("battery_energy_stored", energyStoredHandler);

        // Max energy value sync
        DelegatedIntSH maxEnergyHandler = new DelegatedIntSH(group.getPanel(), slotIndex);
        group.getSyncManager()
            .syncValue("battery_max_energy_" + slotIndex, maxEnergyHandler);
        group.put("battery_max_energy", maxEnergyHandler);
    }
}
