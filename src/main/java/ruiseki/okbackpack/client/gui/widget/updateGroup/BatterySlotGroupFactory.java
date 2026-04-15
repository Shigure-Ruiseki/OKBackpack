package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.slot.ModularUpgradeWidgetSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.value.DelegatedFloatSH;
import ruiseki.okbackpack.client.gui.syncHandler.value.DelegatedIntSH;

public class BatterySlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {
        // Battery inventory handler (2 slots: input, output)
        DelegatedStackHandlerSH batteryInvHandler = new DelegatedStackHandlerSH(
            group.panel::getContainer,
            group.wrapper,
            group.slotIndex,
            2);
        group.syncManager.syncValue("battery_inv_delegation_" + group.slotIndex, batteryInvHandler);
        group.put("battery_inv_handler", batteryInvHandler);

        // Input slot (index 0) - items that provide energy
        ModularUpgradeWidgetSlot inputSlot = new ModularUpgradeWidgetSlot(
            group.slotIndex,
            batteryInvHandler.delegatedStackHandler,
            0);
        inputSlot.slotGroup("battery_slots_" + group.slotIndex);
        group.syncManager.syncValue("battery_slot_" + group.slotIndex, 0, new ItemSlotSH(inputSlot));
        group.put("battery_input", inputSlot);

        // Output slot (index 1) - items that receive energy
        ModularUpgradeWidgetSlot outputSlot = new ModularUpgradeWidgetSlot(
            group.slotIndex,
            batteryInvHandler.delegatedStackHandler,
            1);
        outputSlot.slotGroup("battery_slots_" + group.slotIndex);
        group.syncManager.syncValue("battery_slot_" + group.slotIndex, 1, new ItemSlotSH(outputSlot));
        group.put("battery_output", outputSlot);

        group.syncManager.registerSlotGroup(new SlotGroup("battery_slots_" + group.slotIndex, 2, false));

        // Energy stored value sync
        DelegatedIntSH energyStoredHandler = new DelegatedIntSH(group.wrapper, group.slotIndex);
        group.syncManager.syncValue("battery_energy_stored_" + group.slotIndex, energyStoredHandler);
        group.put("battery_energy_stored", energyStoredHandler);

        // Max energy value sync
        DelegatedIntSH maxEnergyHandler = new DelegatedIntSH(group.wrapper, group.slotIndex);
        group.syncManager.syncValue("battery_max_energy_" + group.slotIndex, maxEnergyHandler);
        group.put("battery_max_energy", maxEnergyHandler);

        // Charge ratio (float 0..1) for progress bar display
        DelegatedFloatSH chargeRatioHandler = new DelegatedFloatSH(group.wrapper, group.slotIndex);
        group.syncManager.syncValue("battery_charge_ratio_" + group.slotIndex, chargeRatioHandler);
        group.put("battery_charge_ratio", chargeRatioHandler);
    }
}
