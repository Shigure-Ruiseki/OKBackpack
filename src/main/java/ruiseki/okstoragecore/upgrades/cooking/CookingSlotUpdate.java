package ruiseki.okstoragecore.upgrades.cooking;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;
import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedFloatSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class CookingSlotUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate group) {

        DelegatedItemHandlerSH cookingHandler = new DelegatedItemHandlerSH(
            group.getPanel(),
            group::getContainer,
            group.getUpgradeIndex(),
            3);
        group.getSyncManager()
            .syncValue("cooking_delegation_" + group.getUpgradeIndex(), cookingHandler);
        group.put("cooking_handler", cookingHandler);

        IndexedModularSlot inputSlot = new IndexedModularSlot(
            cookingHandler.getItemHandler(),
            CookingLogic.COOK_INPUT_SLOT,
            group.getPanel(),
            group.getUpgradeIndex());
        group.getSyncManager()
            .syncValue(
                "cooking_slot_" + group.getUpgradeIndex(),
                CookingLogic.COOK_INPUT_SLOT,
                new ItemSlotSH(inputSlot));
        group.put("cooking_input", inputSlot);

        IndexedModularSlot fuelSlot = new IndexedModularSlot(
            cookingHandler.getItemHandler(),
            CookingLogic.FUEL_SLOT,
            group.getPanel(),
            group.getUpgradeIndex());
        group.getSyncManager()
            .syncValue("cooking_slot_" + group.getUpgradeIndex(), CookingLogic.FUEL_SLOT, new ItemSlotSH(fuelSlot));
        group.put("cooking_fuel", fuelSlot);

        IndexedModularSlot outputSlot = new IndexedModularSlot(
            cookingHandler.getItemHandler(),
            CookingLogic.COOK_OUTPUT_SLOT,
            group.getPanel(),
            group.getUpgradeIndex());
        group.getSyncManager()
            .syncValue(
                "cooking_slot_" + group.getUpgradeIndex(),
                CookingLogic.COOK_OUTPUT_SLOT,
                new ItemSlotSH(outputSlot));
        group.put("cooking_output", outputSlot);

        DelegatedFilterLogicSH logicSH = new DelegatedFilterLogicSH(
            group.getPanel(),
            group::getContainer,
            group.getUpgradeIndex(),
            4);
        group.getSyncManager()
            .syncValue("fuel_filter_delegation_" + group.getUpgradeIndex(), logicSH);
        group.put("fuel_filter_logic", logicSH);

        ModularFilterSlot[] slots = new ModularFilterSlot[4];
        for (int i = 0; i < 4; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(logicSH.getItemHandler(), i);
            slot.slotGroup("fuel_filters_" + group.getUpgradeIndex());
            group.getSyncManager()
                .syncValue("fuel_filter_" + group.getUpgradeIndex(), i, new FilterSlotSH(slot));
            slots[i] = slot;
        }
        group.put("fuel_filter_slots", slots);
        group.getSyncManager()
            .registerSlotGroup(new SlotGroup("fuel_filters_" + group.getUpgradeIndex(), 4, false));

        DelegatedFloatSH cookingProgressHandler = new DelegatedFloatSH(group.getPanel(), group.getUpgradeIndex());
        group.getSyncManager()
            .syncValue("cooking_cook_progress_" + group.getUpgradeIndex(), cookingProgressHandler);
        group.put("cooking_cook_progress", cookingProgressHandler);

        DelegatedFloatSH burnProgressHandler = new DelegatedFloatSH(group.getPanel(), group.getUpgradeIndex());
        group.getSyncManager()
            .syncValue("cooking_burn_progress_" + group.getUpgradeIndex(), burnProgressHandler);
        group.put("cooking_burn_progress", burnProgressHandler);
    }
}
