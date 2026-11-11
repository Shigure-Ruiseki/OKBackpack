package ruiseki.backpack.modcompat.thaumcraft.upgrades.arcane;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.backpack.modcompat.thaumcraft.client.gui.slot.ArcaneCraftingSlot;
import ruiseki.backpack.modcompat.thaumcraft.helper.ThaumcraftHelpers;
import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedBooleanSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedStringSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class ArcaneSlotUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate group) {
        int slotIndex = group.getUpgradeIndex();

        String matrixGroupName = "arcane_matrix_" + slotIndex;
        String resultGroupName = "arcane_result_" + slotIndex;
        String wandGroupName = "arcane_wand_" + slotIndex;

        DelegatedItemHandlerSH arcaneHandler = new DelegatedItemHandlerSH(
            group.getPanel(),
            group::getContainer,
            slotIndex,
            10);

        group.getSyncManager()
            .syncValue("arcane_delegation_" + slotIndex, arcaneHandler);
        group.put("arcane_handler", arcaneHandler);

        group.getSyncManager()
            .registerSlotGroup(new SlotGroup(matrixGroupName, 9, false));

        group.getSyncManager()
            .registerSlotGroup(new SlotGroup(resultGroupName, 1, false));

        group.getSyncManager()
            .registerSlotGroup(new SlotGroup(wandGroupName, 1, false));

        IndexedModularSlot[] slots = new IndexedModularSlot[9];
        for (int i = 0; i < slots.length; i++) {
            IndexedModularSlot slot = new IndexedModularSlot(
                arcaneHandler.getItemHandler(),
                i,
                group.getPanel(),
                group.getUpgradeIndex());
            slot.slotGroup(matrixGroupName);
            group.getSyncManager()
                .syncValue("arcane_slot_" + slotIndex, i, new ItemSlotSH(slot));
            slots[i] = slot;
        }
        group.put("arcane_matrix_slots", slots);

        ArcaneCraftingSlot arcaneResultSlot = new ArcaneCraftingSlot(group.getPanel(), slotIndex);
        arcaneResultSlot.slotGroup(resultGroupName);
        group.getSyncManager()
            .syncValue(resultGroupName, 0, new ItemSlotSH(arcaneResultSlot));
        group.put("arcane_result_slot", arcaneResultSlot);

        IndexedModularSlot wandSlot = new IndexedModularSlot(
            arcaneHandler.getItemHandler(),
            9,
            group.getPanel(),
            slotIndex);
        wandSlot.filter(ThaumcraftHelpers::isWand);
        wandSlot.slotGroup(wandGroupName);
        group.getSyncManager()
            .syncValue(wandGroupName, 0, new ItemSlotSH(wandSlot));
        group.put("arcane_wand_slot", wandSlot);

        DelegatedBooleanSH hasWand = new DelegatedBooleanSH(group.getPanel(), group.getUpgradeIndex());
        group.getSyncManager()
            .syncValue("arcane_has_wand_" + slotIndex, hasWand);
        group.put("arcane_has_wand", hasWand);

        DelegatedStringSH missingResearch = new DelegatedStringSH(group.getPanel(), group.getUpgradeIndex());
        group.getSyncManager()
            .syncValue("arcane_missing_research_" + slotIndex, missingResearch);
        group.put("arcane_missing_research", missingResearch);

        DelegatedStringSH missingResearchName = new DelegatedStringSH(group.getPanel(), group.getUpgradeIndex());
        group.getSyncManager()
            .syncValue("arcane_missing_research_name_" + slotIndex, missingResearchName);
        group.put("arcane_missing_research_name", missingResearchName);

    }
}
