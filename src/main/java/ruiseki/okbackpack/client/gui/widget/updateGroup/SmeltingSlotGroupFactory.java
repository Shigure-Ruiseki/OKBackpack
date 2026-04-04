package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
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

        // Fuel slot (index 1)
        ModularSlot fuelSlot = new ModularSlot(smeltingInvHandler.delegatedStackHandler, 1);
        fuelSlot.slotGroup("smelting_slots_" + group.slotIndex);
        group.syncManager.syncValue("smelting_slot_" + group.slotIndex, 1, new ItemSlotSH(fuelSlot));

        // Output slot (index 2) - extraction only
        ModularSlot outputSlot = new ModularSlot(smeltingInvHandler.delegatedStackHandler, 2);
        outputSlot.slotGroup("smelting_slots_" + group.slotIndex);
        outputSlot.canPut(false);
        group.syncManager.syncValue("smelting_slot_" + group.slotIndex, 2, new ItemSlotSH(outputSlot));

        group.syncManager.registerSlotGroup(new SlotGroup("smelting_slots_" + group.slotIndex, 3, false));

        // Material filter (8 phantom slots) for advanced smelting upgrades
        DelegatedStackHandlerSH smeltingFilterHandler = new DelegatedStackHandlerSH(group.wrapper, group.slotIndex, 8);
        group.syncManager.syncValue("smelting_filter_delegation_" + group.slotIndex, smeltingFilterHandler);
        group.put("smelting_filter_handler", smeltingFilterHandler);

        for (int i = 0; i < 8; i++) {
            ModularFilterSlot filterSlot = new ModularFilterSlot(smeltingFilterHandler.delegatedStackHandler, i);
            filterSlot.slotGroup("smelting_filters_" + group.slotIndex);
            group.syncManager.syncValue("smelting_filter_" + group.slotIndex, i, new FilterSlotSH(filterSlot));
        }
        group.syncManager.registerSlotGroup(new SlotGroup("smelting_filters_" + group.slotIndex, 8, false));

        // Ore dict handler (1 phantom slot) for advanced smelting material filter
        DelegatedStackHandlerSH oreDictHandler = new DelegatedStackHandlerSH(group.wrapper, group.slotIndex, 1);
        group.syncManager.syncValue("smelting_ore_dict_delegation_" + group.slotIndex, oreDictHandler);
        group.put("smelting_ore_dict_handler", oreDictHandler);

        ModularFilterSlot oreDictSlot = new ModularFilterSlot(oreDictHandler.delegatedStackHandler, 0);
        group.syncManager.syncValue("smelting_ore_dict_" + group.slotIndex, 0, new FilterSlotSH(oreDictSlot));

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
    }
}
