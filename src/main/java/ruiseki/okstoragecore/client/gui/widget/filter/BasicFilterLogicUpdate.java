package ruiseki.okstoragecore.client.gui.widget.filter;

import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class BasicFilterLogicUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate group) {

        DelegatedFilterLogicSH logicSH = new DelegatedFilterLogicSH(
            group.getPanel(),
            group::getContainer,
            group.getUpgradeIndex(),
            9);
        group.getSyncManager()
            .syncValue("basic_filter_delegation_" + group.getUpgradeIndex(), logicSH);
        group.put("basic_filter_logic", logicSH);

        ModularFilterSlot[] slots = new ModularFilterSlot[9];
        for (int i = 0; i < 9; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(logicSH.getItemHandler(), i);
            slot.slotGroup("basic_filters_" + group.getUpgradeIndex());
            group.getSyncManager()
                .syncValue("basic_filter_" + group.getUpgradeIndex(), i, new FilterSlotSH(slot));
            slots[i] = slot;
        }

        group.put("basic_filter_slots", slots);
        group.getSyncManager()
            .registerSlotGroup(new SlotGroup("basic_filters_" + group.getUpgradeIndex(), 9, false));
    }
}
