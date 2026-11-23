package ruiseki.okbackpack.modcompat.thaumcraft.upgrades.arcane;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.modcompat.thaumcraft.client.gui.slot.ArcaneCraftingSlot;
import ruiseki.okbackpack.modcompat.thaumcraft.helper.ThaumcraftHelpers;
import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedBooleanSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedStringSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class ArcaneSlotUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate slotUpdate) {
        int slotIndex = slotUpdate.getUpgradeIndex();

        String matrixGroupName = "arcane_matrix_" + slotIndex;
        String resultGroupName = "arcane_result_" + slotIndex;
        String wandGroupName = "arcane_wand_" + slotIndex;

        DelegatedItemHandlerSH arcaneHandler = new DelegatedItemHandlerSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotIndex,
            10);

        slotUpdate.getSyncManager()
            .syncValue("arcane_delegation_" + slotIndex, arcaneHandler);
        slotUpdate.put("arcane_handler", arcaneHandler);

        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup(matrixGroupName, 9, false));

        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup(resultGroupName, 1, false));

        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup(wandGroupName, 1, false));

        IndexedModularSlot[] slots = new IndexedModularSlot[9];
        for (int i = 0; i < slots.length; i++) {
            IndexedModularSlot slot = new IndexedModularSlot(
                arcaneHandler.getItemHandler(),
                i,
                slotUpdate.getPanel(),
                slotUpdate.getUpgradeIndex());
            slot.slotGroup(matrixGroupName);
            slotUpdate.getSyncManager()
                .syncValue("arcane_slot_" + slotIndex, i, new ItemSlotSH(slot));
            slots[i] = slot;
        }
        slotUpdate.put("arcane_matrix_slots", slots);

        ArcaneCraftingSlot arcaneResultSlot = new ArcaneCraftingSlot(slotUpdate.getPanel(), slotIndex);
        arcaneResultSlot.slotGroup(resultGroupName);
        slotUpdate.getSyncManager()
            .syncValue(resultGroupName, 0, new ItemSlotSH(arcaneResultSlot));
        slotUpdate.put("arcane_result_slot", arcaneResultSlot);

        IndexedModularSlot wandSlot = new IndexedModularSlot(
            arcaneHandler.getItemHandler(),
            9,
            slotUpdate.getPanel(),
            slotIndex);
        wandSlot.filter(ThaumcraftHelpers::isWand);
        wandSlot.slotGroup(wandGroupName);
        slotUpdate.getSyncManager()
            .syncValue(wandGroupName, 0, new ItemSlotSH(wandSlot));
        slotUpdate.put("arcane_wand_slot", wandSlot);

        DelegatedBooleanSH hasWand = new DelegatedBooleanSH(slotUpdate.getPanel(), slotUpdate.getUpgradeIndex());
        slotUpdate.getSyncManager()
            .syncValue("arcane_has_wand_" + slotIndex, hasWand);
        slotUpdate.put("arcane_has_wand", hasWand);

        DelegatedStringSH missingResearch = new DelegatedStringSH(slotUpdate.getPanel(), slotUpdate.getUpgradeIndex());
        slotUpdate.getSyncManager()
            .syncValue("arcane_missing_research_" + slotIndex, missingResearch);
        slotUpdate.put("arcane_missing_research", missingResearch);

        DelegatedStringSH missingResearchName = new DelegatedStringSH(
            slotUpdate.getPanel(),
            slotUpdate.getUpgradeIndex());
        slotUpdate.getSyncManager()
            .syncValue("arcane_missing_research_name_" + slotIndex, missingResearchName);
        slotUpdate.put("arcane_missing_research_name", missingResearchName);

    }
}
