package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedFloatSH;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;

public class SmeltingSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {
        // Smelting inventory handler (3 slots: input, fuel, output)
        DelegatedStackHandlerSH smeltingInvHandler = new DelegatedStackHandlerSH(group.wrapper, group.slotIndex, 3);
        group.syncManager.syncValue("smelting_inv_delegation_" + group.slotIndex, smeltingInvHandler);
        group.put("smelting_inv_handler", smeltingInvHandler);

        // Input slot (index 0)
        ModularSlot inputSlot = new ModularSlot(smeltingInvHandler.delegatedStackHandler, 0);
        inputSlot.slotGroup("smelting_slots_" + group.slotIndex);
        group.syncManager.syncValue("smelting_slot_" + group.slotIndex, 0, new ItemSlotSH(inputSlot));
        group.put("smelting_input", inputSlot);

        // Fuel slot (index 1)
        ModularSlot fuelSlot = new ModularSlot(smeltingInvHandler.delegatedStackHandler, 1);
        fuelSlot.slotGroup("smelting_slots_" + group.slotIndex);
        group.syncManager.syncValue("smelting_slot_" + group.slotIndex, 1, new ItemSlotSH(fuelSlot));
        group.put("smelting_fuel", fuelSlot);

        // Output slot (index 2) - extraction only
        ModularSlot outputSlot = new ModularSlot(smeltingInvHandler.delegatedStackHandler, 2);
        outputSlot.slotGroup("smelting_slots_" + group.slotIndex);
        outputSlot.canPut(false);
        group.syncManager.syncValue("smelting_slot_" + group.slotIndex, 2, new ItemSlotSH(outputSlot));
        group.put("smelting_output", outputSlot);

        group.syncManager.registerSlotGroup(new SlotGroup("smelting_slots_" + group.slotIndex, 3, false));

        // Fuel filter (4 phantom slots) for advanced smelting upgrades
        DelegatedStackHandlerSH fuelFilterHandler = new DelegatedStackHandlerSH(group.wrapper, group.slotIndex, 4);
        group.syncManager.syncValue("fuel_filter_delegation_" + group.slotIndex, fuelFilterHandler);
        group.put("fuel_filter_handler", fuelFilterHandler);

        for (int i = 0; i < 4; i++) {
            ModularFilterSlot fuelSlotFilter = new ModularFilterSlot(fuelFilterHandler.delegatedStackHandler, i);
            fuelSlotFilter.slotGroup("fuel_filters_" + group.slotIndex);
            group.syncManager.syncValue("fuel_filter_" + group.slotIndex, i, new FilterSlotSH(fuelSlotFilter));
        }
        group.syncManager.registerSlotGroup(new SlotGroup("fuel_filters_" + group.slotIndex, 4, false));

        DelegatedFloatSH progressHandler = new DelegatedFloatSH(group.wrapper, group.slotIndex);
        group.syncManager.syncValue("smelting_progress_handler_" + group.slotIndex, progressHandler);
        group.put("smelting_progress_handler", progressHandler);

        DelegatedFloatSH fuelHandler = new DelegatedFloatSH(group.wrapper, group.slotIndex);
        group.syncManager.syncValue("smelting_fuel_handler_" + group.slotIndex, fuelHandler);
        group.put("smelting_fuel_handler", fuelHandler);
    }
}
