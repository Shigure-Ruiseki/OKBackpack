package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;

public class CommonFilterSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {

        DelegatedStackHandlerSH handler = new DelegatedStackHandlerSH(
            group.panel::getContainer,
            group.wrapper,
            group.slotIndex,
            9);

        group.syncManager.syncValue("common_filter_delegation_" + group.slotIndex, handler);

        group.put("common_filter_handler", handler);

        ModularFilterSlot[] slots = new ModularFilterSlot[9];
        for (int i = 0; i < 9; i++) {

            ModularFilterSlot slot = new ModularFilterSlot(handler.delegatedStackHandler, i);

            slot.slotGroup("common_filters_" + group.slotIndex);

            group.syncManager.syncValue("common_filter_" + group.slotIndex, i, new FilterSlotSH(slot));

            slots[i] = slot;
        }

        group.put("common_filter_slots", slots);
        group.syncManager.registerSlotGroup(new SlotGroup("common_filters_" + group.slotIndex, 9, false));
    }
}
