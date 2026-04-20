package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;
import ruiseki.okbackpack.client.gui.syncHandler.OreDictFilterSlotSH;

public class AdvancedRestockFilterSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {

        DelegatedStackHandlerSH advancedRestockFilterStackHandler = new DelegatedStackHandlerSH(
            group.panel::getContainer,
            group.wrapper,
            group.slotIndex,
            16);
        group.syncManager
            .syncValue("adv_restock_filter_delegation_" + group.slotIndex, advancedRestockFilterStackHandler);
        group.put("adv_restock_filter_handler", advancedRestockFilterStackHandler);

        ModularFilterSlot[] slots = new ModularFilterSlot[16];
        for (int i = 0; i < 16; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(advancedRestockFilterStackHandler.delegatedStackHandler, i);
            slot.slotGroup("adv_restock_filters_" + group.slotIndex);

            group.syncManager.syncValue("adv_restock_filter_" + group.slotIndex, i, new FilterSlotSH(slot));
        }
        group.put("adv_restock_filter_slots", slots);

        group.syncManager.registerSlotGroup(new SlotGroup("adv_restock_filters_" + group.slotIndex, 16, false));

        DelegatedStackHandlerSH oreDictStackHandler = new DelegatedStackHandlerSH(
            group.panel::getContainer,
            group.wrapper,
            group.slotIndex,
            1);

        group.syncManager.syncValue("ore_dict_delegation_" + group.slotIndex, oreDictStackHandler);
        group.put("ore_dict_handler", oreDictStackHandler);

        ModularFilterSlot oreDictSlot = new ModularFilterSlot(oreDictStackHandler.delegatedStackHandler, 0);
        group.syncManager.syncValue("ore_dict_" + group.slotIndex, 0, new OreDictFilterSlotSH(oreDictSlot));
        group.put("ore_dict_slots", slots);
    }
}
