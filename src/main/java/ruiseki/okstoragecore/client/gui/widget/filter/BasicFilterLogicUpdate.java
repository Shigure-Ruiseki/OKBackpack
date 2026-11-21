package ruiseki.okstoragecore.client.gui.widget.filter;

import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class BasicFilterLogicUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate slotUpdate) {

        DelegatedFilterLogicSH logicSH = new DelegatedFilterLogicSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotUpdate.getUpgradeIndex(),
            9);
        slotUpdate.getSyncManager()
            .syncValue("basic_filter_delegation_" + slotUpdate.getUpgradeIndex(), logicSH);
        slotUpdate.put("basic_filter_logic", logicSH);

        ModularFilterSlot[] slots = new ModularFilterSlot[9];
        for (int i = 0; i < 9; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(logicSH.getItemHandler(), i);
            slot.slotGroup("basic_filters_" + slotUpdate.getUpgradeIndex());
            slotUpdate.getSyncManager()
                .syncValue("basic_filter_" + slotUpdate.getUpgradeIndex(), i, new FilterSlotSH(slot));
            slots[i] = slot;
        }

        slotUpdate.put("basic_filter_slots", slots);
        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup("basic_filters_" + slotUpdate.getUpgradeIndex(), 9, false));
    }
}
