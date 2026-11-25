package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;

public class AdvancedFeedingFilterSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {

        DelegatedStackHandlerSH commonFilterStackHandler = group.get("adv_common_filter_handler");

        ModularFilterSlot[] slots = new ModularFilterSlot[16];
        for (int i = 0; i < 16; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(commonFilterStackHandler.delegatedStackHandler, i);
            slot.slotGroup("adv_feeding_filters_" + group.slotIndex);
            group.syncManager.syncValue("adv_feeding_filter_" + group.slotIndex, i, new FilterSlotSH(slot));
        }
        group.put("adv_feeding_filter_slots", slots);

        group.syncManager.registerSlotGroup(new SlotGroup("adv_feeding_filters_" + group.slotIndex, 9, false));
    }
}
