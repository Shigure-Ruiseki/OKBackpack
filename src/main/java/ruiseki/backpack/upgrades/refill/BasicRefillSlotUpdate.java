package ruiseki.backpack.upgrades.refill;

import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class BasicRefillSlotUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate slotUpdate) {
        int slotIndex = slotUpdate.getUpgradeIndex();

        DelegatedFilterLogicSH basicRefillHandler = new DelegatedFilterLogicSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotIndex,
            6);

        slotUpdate.getSyncManager()
            .syncValue("basic_refill_delegation_" + slotIndex, basicRefillHandler);
        slotUpdate.put("basic_refill_logic", basicRefillHandler);

        ModularFilterSlot[] basicSlots = new ModularFilterSlot[6];
        for (int i = 0; i < 6; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(basicRefillHandler.getItemHandler(), i);
            slot.slotGroup("basic_refills_" + slotUpdate.getUpgradeIndex());
            slotUpdate.getSyncManager()
                .syncValue("basic_refill_" + slotUpdate.getUpgradeIndex(), i, new FilterSlotSH(slot));
            basicSlots[i] = slot;
        }
        slotUpdate.put("basic_refill_slots", basicSlots);
        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup("basic_refills_" + slotUpdate.getUpgradeIndex(), 6, false));
    }
}
