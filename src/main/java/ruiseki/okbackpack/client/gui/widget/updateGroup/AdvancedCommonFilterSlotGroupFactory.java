package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;

public class AdvancedCommonFilterSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {

        DelegatedStackHandlerSH advancedCommonFilterStackHandler = new DelegatedStackHandlerSH(
            group.wrapper,
            group.slotIndex,
            16);
        group.syncManager
            .syncValue("adv_common_filter_delegation_" + group.slotIndex, advancedCommonFilterStackHandler);
        group.put("adv_common_filter_handler", advancedCommonFilterStackHandler);

        ModularFilterSlot[] slots = new ModularFilterSlot[16];
        for (int i = 0; i < 16; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(advancedCommonFilterStackHandler.delegatedStackHandler, i);
            slot.slotGroup("adv_common_filters_" + group.slotIndex);

            group.syncManager.syncValue("adv_common_filter_" + group.slotIndex, i, new FilterSlotSH(slot));
        }
        group.put("adv_common_filter_slots", slots);

        group.syncManager.registerSlotGroup(new SlotGroup("adv_common_filters_" + group.slotIndex, 16, false));

        DelegatedStackHandlerSH oreDictStackHandler = new DelegatedStackHandlerSH(group.wrapper, group.slotIndex, 1);

        group.syncManager.syncValue("ore_dict_delegation_" + group.slotIndex, oreDictStackHandler);
        group.put("ore_dict_handler", oreDictStackHandler);

        ModularFilterSlot oreDictSlot = new ModularFilterSlot(oreDictStackHandler.delegatedStackHandler, 0);
        group.syncManager.syncValue("ore_dict_" + group.slotIndex, 0, new FilterSlotSH(oreDictSlot));
        group.put("ore_dict_", slots);
    }
}
