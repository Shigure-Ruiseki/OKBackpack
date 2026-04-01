package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularCraftingSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.slot.CraftingSlotInfo;
import ruiseki.okbackpack.client.gui.slot.IndexedModularCraftingMatrixSlot;
import ruiseki.okbackpack.client.gui.slot.IndexedModularCraftingSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedCraftingStackHandlerSH;

public class CraftingSlotGroup implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {

        DelegatedCraftingStackHandlerSH craftingStackHandler = new DelegatedCraftingStackHandlerSH(
            group.panel::getBackpackContainer,
            group.wrapper,
            group.slotIndex,
            10);
        group.syncManager.syncValue("crafting_delegation_" + group.slotIndex, craftingStackHandler);
        group.put("crafting_handler", craftingStackHandler);

        ModularSlot[] slots = new ModularSlot[9];
        for (int i = 0; i < 9; i++) {
            ModularSlot slot = new IndexedModularCraftingMatrixSlot(
                group.slotIndex,
                craftingStackHandler.delegatedStackHandler,
                i);
            slot.slotGroup("crafting_result_" + group.slotIndex);
            group.syncManager.syncValue("crafting_slot_" + group.slotIndex, i, new ItemSlotSH(slot));
            slots[i] = slot;

            slot.changeListener((stack, onlyAmountChanged, client, init) -> {

                if (!client) return;

                craftingStackHandler.syncToServer(DelegatedCraftingStackHandlerSH.DETECT_CHANGES);
            });
        }
        group.put("crafting_matrix_slots", slots);

        group.syncManager.registerSlotGroup(new SlotGroup("crafting_matrix_" + group.slotIndex, 3, false));

        ModularCraftingSlot craftingOutputSlot = new IndexedModularCraftingSlot(
            group.slotIndex,
            group.wrapper,
            craftingStackHandler.delegatedStackHandler,
            9);
        craftingOutputSlot.slotGroup("crafting_result_" + group.slotIndex);
        group.syncManager.syncValue("crafting_result_" + group.slotIndex, 0, new ItemSlotSH(craftingOutputSlot));
        group.put("crafting_result_slot", craftingOutputSlot);

        group.syncManager.registerSlotGroup(new SlotGroup("crafting_result_" + group.slotIndex, 1, false));

        CraftingSlotInfo craftingInfo = new CraftingSlotInfo(slots, craftingOutputSlot);
        group.put("crafting_info", craftingInfo);

    }
}
