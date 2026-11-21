package ruiseki.backpack.upgrades.refill;

import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.client.gui.slot.ModularFilterSlot;
import ruiseki.okbackpack.client.gui.syncHandler.FilterSlotSH;
import ruiseki.okstoragecore.client.gui.sync.DelegatedFilterLogicSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class AdvancedRefillSlotUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate slotUpdate) {
        int slotIndex = slotUpdate.getUpgradeIndex();

        DelegatedFilterLogicSH advancedRefillHandler = new DelegatedFilterLogicSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotIndex,
            12);

        slotUpdate.getSyncManager()
            .syncValue("adv_refill_delegation_" + slotIndex, advancedRefillHandler);
        slotUpdate.put("adv_refill_logic", advancedRefillHandler);

        ModularFilterSlot[] advancedSlots = new ModularFilterSlot[12];
        for (int i = 0; i < 12; i++) {
            ModularFilterSlot slot = new ModularFilterSlot(advancedRefillHandler.getItemHandler(), i);
            slot.slotGroup("adv_refills_" + slotUpdate.getUpgradeIndex());
            slotUpdate.getSyncManager()
                .syncValue("adv_refill_" + slotUpdate.getUpgradeIndex(), i, new FilterSlotSH(slot));
            advancedSlots[i] = slot;
        }
        slotUpdate.put("adv_refill_slots", advancedSlots);
        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup("adv_refills_" + slotUpdate.getUpgradeIndex(), 12, false));
    }
}
