package ruiseki.okstoragecore.client.gui.widget.filter;

import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;
import ruiseki.okcore.item.handler.ItemStackHandler;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class AdvancedFilterLogicUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate group) {

        DelegatedFilterLogicSH logicSH = new DelegatedFilterLogicSH(
            group.getPanel(),
            group::getContainer,
            group.getUpgradeIndex(),
            16);
        group.getSyncManager()
            .syncValue("adv_filter_delegation_" + group.getUpgradeIndex(), logicSH);
        group.put("adv_filter_logic", logicSH);

        ModularFilterSlot[] slots = new ModularFilterSlot[16];
        for (int i = 0; i < 16; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(logicSH.getItemHandler(), i);
            slot.slotGroup("adv_filters_" + group.getUpgradeIndex());
            group.getSyncManager()
                .syncValue("adv_filter_" + group.getUpgradeIndex(), i, new FilterSlotSH(slot));
            slots[i] = slot;
        }
        group.put("adv_filter_slots", slots);
        group.getSyncManager()
            .registerSlotGroup(new SlotGroup("adv_filters_" + group.getUpgradeIndex(), 16, false));

        ModularFilterSlot oreDict = new ModularFilterSlot(new ItemStackHandler(1), 0);
        group.getSyncManager()
            .syncValue("adv_filter_oredict_" + group.getUpgradeIndex(), 0, new FilterSlotSH(oreDict));
        group.put("adv_filter_oredict", oreDict);
    }
}
