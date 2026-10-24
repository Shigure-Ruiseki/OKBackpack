package ruiseki.okstoragecore.client.gui.widget.filter;

import com.cleanroommc.modularui.value.sync.PhantomItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class BasicFilterLogicUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate group) {

        DelegatedFilterLogicSH logicSH = new DelegatedFilterLogicSH(
            group.getWrapper(),
            group.getPanel(),
            group::getContainer,
            group.getSlotIndex(),
            9);
        group.getSyncManager()
            .syncValue("basic_filter_delegation_" + group.getSlotIndex(), logicSH);
        group.put("basic_filter_logic", logicSH);

        ModularFilterSlot[] slots = new ModularFilterSlot[9];
        for (int i = 0; i < 9; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(logicSH.getItemHandler(), i);
            slot.slotGroup("basic_filters_" + group.getSlotIndex());
            group.getSyncManager()
                .syncValue("basic_filter_" + group.getSlotIndex(), i, new PhantomItemSlotSH(slot));
            slots[i] = slot;
        }

        group.put("basic_filter_slots", slots);
        group.getSyncManager()
            .registerSlotGroup(new SlotGroup("basic_filters_" + group.getSlotIndex(), 9, false));
    }
}
