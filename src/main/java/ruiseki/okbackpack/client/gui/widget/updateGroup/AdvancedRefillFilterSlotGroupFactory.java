package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;

public class AdvancedRefillFilterSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {

        DelegatedStackHandlerSH handler = new DelegatedStackHandlerSH(
            group.panel::getContainer,
            group.wrapper,
            group.slotIndex,
            16);

        group.syncManager.syncValue("adv_refill_filter_delegation_" + group.slotIndex, handler);
        group.put("adv_refill_filter_handler", handler);

        ModularFilterSlot[] slots = new ModularFilterSlot[16];
        for (int i = 0; i < 16; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(handler.delegatedStackHandler, i);
            slot.slotGroup("adv_refill_filters_" + group.slotIndex);

            group.syncManager.syncValue("adv_refill_filter_" + group.slotIndex, i, new FilterSlotSH(slot));

            slots[i] = slot;
        }

        group.put("adv_refill_filter_slots", slots);
        group.syncManager.registerSlotGroup(new SlotGroup("adv_refill_filters_" + group.slotIndex, 16, false));
    }
}
