package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;

public class FeedingFilterSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {

        DelegatedStackHandlerSH commonFilterStackHandler = group.get("common_filter_handler");

        ModularFilterSlot[] slots = new ModularFilterSlot[9];
        for (int i = 0; i < 9; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(commonFilterStackHandler.delegatedStackHandler, i);
            slot.slotGroup("feeding_filters_" + group.slotIndex);
            group.syncManager.syncValue("feeding_filter_" + group.slotIndex, i, new FilterSlotSH(slot));
        }
        group.put("feeding_filter_slots", slots);

        group.syncManager.registerSlotGroup(new SlotGroup("feeding_filters_" + group.slotIndex, 9, false));
    }
}
