package ruiseki.okbackpack.client.gui.widget.updateGroup;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.slot.ModularUpgradeWidgetSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.value.DelegatedFloatSH;
import ruiseki.okbackpack.client.gui.syncHandler.value.DelegatedIntSH;

public class TankSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {
        // Tank inventory handler (4 slots: input, output, input_result, output_result)
        DelegatedStackHandlerSH tankInvHandler = new DelegatedStackHandlerSH(
            group.panel::getContainer,
            group.wrapper,
            group.slotIndex,
            4);
        group.syncManager.syncValue("tank_inv_delegation_" + group.slotIndex, tankInvHandler);
        group.put("tank_inv_handler", tankInvHandler);

        // Input slot (index 0) - containers that provide fluid
        ModularUpgradeWidgetSlot inputSlot = new ModularUpgradeWidgetSlot(
            group.slotIndex,
            tankInvHandler.delegatedStackHandler,
            0);
        inputSlot.slotGroup("tank_slots_" + group.slotIndex);
        group.syncManager.syncValue("tank_slot_" + group.slotIndex, 0, new ItemSlotSH(inputSlot));
        group.put("tank_input", inputSlot);

        // Output slot (index 1) - containers that receive fluid
        ModularUpgradeWidgetSlot outputSlot = new ModularUpgradeWidgetSlot(
            group.slotIndex,
            tankInvHandler.delegatedStackHandler,
            1);
        outputSlot.slotGroup("tank_slots_" + group.slotIndex);
        group.syncManager.syncValue("tank_slot_" + group.slotIndex, 1, new ItemSlotSH(outputSlot));
        group.put("tank_output", outputSlot);

        // Input result slot (index 2) - emptied containers
        ModularSlot inputResultSlot = new ModularSlot(tankInvHandler.delegatedStackHandler, 2) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        };
        inputResultSlot.slotGroup("tank_slots_" + group.slotIndex);
        group.syncManager.syncValue("tank_slot_" + group.slotIndex, 2, new ItemSlotSH(inputResultSlot));
        group.put("tank_input_result", inputResultSlot);

        // Output result slot (index 3) - filled containers
        ModularSlot outputResultSlot = new ModularSlot(tankInvHandler.delegatedStackHandler, 3) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        };
        outputResultSlot.slotGroup("tank_slots_" + group.slotIndex);
        group.syncManager.syncValue("tank_slot_" + group.slotIndex, 3, new ItemSlotSH(outputResultSlot));
        group.put("tank_output_result", outputResultSlot);

        group.syncManager.registerSlotGroup(new SlotGroup("tank_slots_" + group.slotIndex, 4, false));

        // Fluid amount value sync
        DelegatedIntSH fluidAmountHandler = new DelegatedIntSH(group.wrapper, group.slotIndex);
        group.syncManager.syncValue("tank_fluid_amount_" + group.slotIndex, fluidAmountHandler);
        group.put("tank_fluid_amount", fluidAmountHandler);

        // Tank capacity value sync
        DelegatedIntSH tankCapacityHandler = new DelegatedIntSH(group.wrapper, group.slotIndex);
        group.syncManager.syncValue("tank_capacity_" + group.slotIndex, tankCapacityHandler);
        group.put("tank_capacity", tankCapacityHandler);

        // Fill ratio (float 0..1) for bar display
        DelegatedFloatSH fillRatioHandler = new DelegatedFloatSH(group.wrapper, group.slotIndex);
        group.syncManager.syncValue("tank_fill_ratio_" + group.slotIndex, fillRatioHandler);
        group.put("tank_fill_ratio", fillRatioHandler);

        // Fluid ID for display (int, -1 = empty)
        DelegatedIntSH fluidIdHandler = new DelegatedIntSH(group.wrapper, group.slotIndex);
        group.syncManager.syncValue("tank_fluid_id_" + group.slotIndex, fluidIdHandler);
        group.put("tank_fluid_id", fluidIdHandler);
    }
}
