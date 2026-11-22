package ruiseki.okstoragecore.upgrades.cooking;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.slot.ModularFilterSlot;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.FilterSlotSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedFloatSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class CookingSlotUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate slotUpdate) {

        DelegatedItemHandlerSH cookingHandler = new DelegatedItemHandlerSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotUpdate.getUpgradeIndex(),
            3);
        slotUpdate.getSyncManager()
            .syncValue("cooking_delegation_" + slotUpdate.getUpgradeIndex(), cookingHandler);
        slotUpdate.put("cooking_handler", cookingHandler);

        IndexedModularSlot inputSlot = new IndexedModularSlot(
            cookingHandler.getItemHandler(),
            CookingLogic.COOK_INPUT_SLOT,
            slotUpdate.getPanel(),
            slotUpdate.getUpgradeIndex());
        slotUpdate.getSyncManager()
            .syncValue(
                "cooking_slot_" + slotUpdate.getUpgradeIndex(),
                CookingLogic.COOK_INPUT_SLOT,
                new ItemSlotSH(inputSlot));
        slotUpdate.put("cooking_input", inputSlot);

        IndexedModularSlot fuelSlot = new IndexedModularSlot(
            cookingHandler.getItemHandler(),
            CookingLogic.FUEL_SLOT,
            slotUpdate.getPanel(),
            slotUpdate.getUpgradeIndex());
        slotUpdate.getSyncManager()
            .syncValue(
                "cooking_slot_" + slotUpdate.getUpgradeIndex(),
                CookingLogic.FUEL_SLOT,
                new ItemSlotSH(fuelSlot));
        slotUpdate.put("cooking_fuel", fuelSlot);

        IndexedModularSlot outputSlot = new IndexedModularSlot(
            cookingHandler.getItemHandler(),
            CookingLogic.COOK_OUTPUT_SLOT,
            slotUpdate.getPanel(),
            slotUpdate.getUpgradeIndex());
        outputSlot.canPut(false);
        slotUpdate.getSyncManager()
            .syncValue(
                "cooking_slot_" + slotUpdate.getUpgradeIndex(),
                CookingLogic.COOK_OUTPUT_SLOT,
                new ItemSlotSH(outputSlot));
        slotUpdate.put("cooking_output", outputSlot);

        DelegatedFilterLogicSH logicSH = new DelegatedFilterLogicSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotUpdate.getUpgradeIndex(),
            4);
        slotUpdate.getSyncManager()
            .syncValue("fuel_filter_delegation_" + slotUpdate.getUpgradeIndex(), logicSH);
        slotUpdate.put("fuel_filter_logic", logicSH);

        ModularFilterSlot[] slots = new ModularFilterSlot[4];
        for (int i = 0; i < 4; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(logicSH.getItemHandler(), i);
            slot.slotGroup("fuel_filters_" + slotUpdate.getUpgradeIndex());
            slotUpdate.getSyncManager()
                .syncValue("fuel_filter_" + slotUpdate.getUpgradeIndex(), i, new FilterSlotSH(slot));
            slots[i] = slot;
        }
        slotUpdate.put("fuel_filter_slots", slots);
        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup("fuel_filters_" + slotUpdate.getUpgradeIndex(), 4, false));

        DelegatedFloatSH cookingProgressHandler = new DelegatedFloatSH(
            slotUpdate.getPanel(),
            slotUpdate.getUpgradeIndex());
        slotUpdate.getSyncManager()
            .syncValue("cooking_cook_progress_" + slotUpdate.getUpgradeIndex(), cookingProgressHandler);
        slotUpdate.put("cooking_cook_progress", cookingProgressHandler);

        DelegatedFloatSH burnProgressHandler = new DelegatedFloatSH(
            slotUpdate.getPanel(),
            slotUpdate.getUpgradeIndex());
        slotUpdate.getSyncManager()
            .syncValue("cooking_burn_progress_" + slotUpdate.getUpgradeIndex(), burnProgressHandler);
        slotUpdate.put("cooking_burn_progress", burnProgressHandler);
    }
}
