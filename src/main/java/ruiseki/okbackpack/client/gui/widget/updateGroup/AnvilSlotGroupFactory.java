package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.slot.AnvilOutputModularSlot;
import ruiseki.okbackpack.client.gui.slot.ModularUpgradeWidgetSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;

public class AnvilSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {
        // Anvil inventory handler (3 slots: left input, right input, output)
        DelegatedStackHandlerSH anvilInvHandler = new DelegatedStackHandlerSH(
            group.panel::getContainer,
            group.wrapper,
            group.slotIndex,
            3);
        group.syncManager.syncValue("anvil_inv_delegation_" + group.slotIndex, anvilInvHandler);
        group.put("anvil_inv_handler", anvilInvHandler);

        // Left input slot (index 0)
        ModularUpgradeWidgetSlot leftInputSlot = new ModularUpgradeWidgetSlot(
            group.slotIndex,
            anvilInvHandler.delegatedStackHandler,
            0);
        leftInputSlot.slotGroup("anvil_slots_" + group.slotIndex);
        group.syncManager.syncValue("anvil_slot_" + group.slotIndex, 0, new ItemSlotSH(leftInputSlot));
        group.put("anvil_left_input", leftInputSlot);

        // Right input slot (index 1)
        ModularUpgradeWidgetSlot rightInputSlot = new ModularUpgradeWidgetSlot(
            group.slotIndex,
            anvilInvHandler.delegatedStackHandler,
            1);
        rightInputSlot.slotGroup("anvil_slots_" + group.slotIndex);
        group.syncManager.syncValue("anvil_slot_" + group.slotIndex, 1, new ItemSlotSH(rightInputSlot));
        group.put("anvil_right_input", rightInputSlot);

        // Output slot (index 2) - extraction only via custom slot
        AnvilOutputModularSlot outputSlot = new AnvilOutputModularSlot(
            anvilInvHandler.delegatedStackHandler,
            2,
            group.wrapper,
            group.slotIndex);
        outputSlot.slotGroup("anvil_slots_" + group.slotIndex);
        outputSlot.canPut(false);
        group.syncManager.syncValue("anvil_slot_" + group.slotIndex, 2, new ItemSlotSH(outputSlot));
        group.put("anvil_output", outputSlot);

        group.syncManager.registerSlotGroup(new SlotGroup("anvil_slots_" + group.slotIndex, 3, false));
    }
}
