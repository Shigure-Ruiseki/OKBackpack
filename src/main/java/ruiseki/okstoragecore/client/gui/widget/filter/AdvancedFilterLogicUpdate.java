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
    public void build(UpgradeSlotUpdate slotUpdate) {

        DelegatedFilterLogicSH logicSH = new DelegatedFilterLogicSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotUpdate.getUpgradeIndex(),
            16);
        slotUpdate.getSyncManager()
            .syncValue("adv_filter_delegation_" + slotUpdate.getUpgradeIndex(), logicSH);
        slotUpdate.put("adv_filter_logic", logicSH);

        ModularFilterSlot[] slots = new ModularFilterSlot[16];
        for (int i = 0; i < 16; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(logicSH.getItemHandler(), i);
            slot.slotGroup("adv_filters_" + slotUpdate.getUpgradeIndex());
            slotUpdate.getSyncManager()
                .syncValue("adv_filter_" + slotUpdate.getUpgradeIndex(), i, new FilterSlotSH(slot));
            slots[i] = slot;
        }
        slotUpdate.put("adv_filter_slots", slots);
        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup("adv_filters_" + slotUpdate.getUpgradeIndex(), 16, false));

        ModularFilterSlot oreDict = new ModularFilterSlot(new ItemStackHandler(1), 0);
        slotUpdate.getSyncManager()
            .syncValue("adv_filter_oredict_" + slotUpdate.getUpgradeIndex(), 0, new FilterSlotSH(oreDict));
        slotUpdate.put("adv_filter_oredict", oreDict);
    }
}
