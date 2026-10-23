
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
    public void build(UpgradeSlotUpdate group) {
        int slotIndex = group.getSlotIndex();

        String matrixGroupName = "crafting_matrix_" + slotIndex;
        String resultGroupName = "crafting_result_" + slotIndex;

        DelegatedItemHandlerSH craftingHandler = new DelegatedItemHandlerSH(
            group.getWrapper(),
            group.getPanel(),
            group::getContainer,
            slotIndex,
            10);

        group.getSyncManager()
            .syncValue("crafting_delegation_" + slotIndex, craftingHandler);
        group.put("crafting_handler", craftingHandler);

        group.getSyncManager()
            .registerSlotGroup(new SlotGroup(matrixGroupName, 9, false));

        group.getSyncManager()
            .registerSlotGroup(new SlotGroup(resultGroupName, 1, false));

        IndexedModularSlot[] slots = new IndexedModularSlot[9];
        for (int i = 0; i < slots.length; i++) {
            IndexedModularSlot slot = new IndexedModularSlot(
                craftingHandler.getItemHandler(),
                i,
                group.getWrapper(),
                group.getPanel(),
                group.getSlotIndex());
            slot.slotGroup(matrixGroupName);
            group.getSyncManager()
                .syncValue("crafting_slot_" + slotIndex, i, new ItemSlotSH(slot));
            slots[i] = slot;
        }

        group.put("crafting_matrix_slots", slots);

        ModularCraftingSlot craftingResultSlot = new ModularCraftingSlot(
            group.getWrapper(),
            group.getPanel(),
            slotIndex);

        craftingResultSlot.slotGroup(resultGroupName);

        group.getSyncManager()
            .syncValue(resultGroupName, 0, new ItemSlotSH(craftingResultSlot));

        group.put("crafting_result_slot", craftingResultSlot);

        CraftingContainer container = new CraftingContainer(
            group.getWrapper(),
            group.getPanel(),
            craftingHandler.getContainerSupplier(),
            group.getPlayer(),
            group.getSlotIndex(),
            3,
            3,
            craftingHandler.getItemHandler());
        group.setUpgradeContainer(container);
    }
}
