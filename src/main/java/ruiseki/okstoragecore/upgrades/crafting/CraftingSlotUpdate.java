
package ruiseki.okstoragecore.upgrades.crafting;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.slot.ModularCraftingSlot;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class CraftingSlotUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate slotUpdate) {
        int slotIndex = slotUpdate.getUpgradeIndex();

        String matrixGroupName = "crafting_matrix_" + slotIndex;
        String resultGroupName = "crafting_result_" + slotIndex;

        DelegatedItemHandlerSH craftingHandler = new DelegatedItemHandlerSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotIndex,
            9);

        slotUpdate.getSyncManager()
            .syncValue("crafting_delegation_" + slotIndex, craftingHandler);
        slotUpdate.put("crafting_handler", craftingHandler);

        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup(matrixGroupName, 9, false));

        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup(resultGroupName, 1, false));

        IndexedModularSlot[] slots = new IndexedModularSlot[9];
        for (int i = 0; i < slots.length; i++) {
            IndexedModularSlot slot = new IndexedModularSlot(
                craftingHandler.getItemHandler(),
                i,
                slotUpdate.getPanel(),
                slotUpdate.getUpgradeIndex());
            slot.slotGroup(matrixGroupName);
            slotUpdate.getSyncManager()
                .syncValue("crafting_slot_" + slotIndex, i, new ItemSlotSH(slot));
            slots[i] = slot;
        }

        slotUpdate.put("crafting_matrix_slots", slots);

        ModularCraftingSlot craftingResultSlot = new ModularCraftingSlot(slotUpdate.getPanel(), slotIndex);

        craftingResultSlot.slotGroup(resultGroupName);

        slotUpdate.getSyncManager()
            .syncValue(resultGroupName, 0, new ItemSlotSH(craftingResultSlot));

        slotUpdate.put("crafting_result_slot", craftingResultSlot);
    }
}
