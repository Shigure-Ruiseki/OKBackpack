package ruiseki.okstoragecore.upgrades.jukebox;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedBooleanSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedIntSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class JukeboxSlotUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate slotUpdate) {
        int slotIndex = slotUpdate.getUpgradeIndex();

        String jukebox_slotGroup = "jukebox_" + slotIndex;

        DelegatedItemHandlerSH jukeboxHandler = new DelegatedItemHandlerSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotIndex,
            16);

        slotUpdate.getSyncManager()
            .syncValue("jukebox_delegation_" + slotIndex, jukeboxHandler);
        slotUpdate.put("jukebox_handler", jukeboxHandler);

        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup(jukebox_slotGroup, 16, false));

        IndexedModularSlot[] slots = new IndexedModularSlot[16];
        for (int i = 0; i < slots.length; i++) {
            IndexedModularSlot slot = new IndexedModularSlot(
                jukeboxHandler.getItemHandler(),
                i,
                slotUpdate.getPanel(),
                slotUpdate.getUpgradeIndex());
            slot.slotGroup(jukebox_slotGroup);
            slotUpdate.getSyncManager()
                .syncValue("jukebox_slot_" + slotIndex, i, new ItemSlotSH(slot));
            slots[i] = slot;
        }
        slotUpdate.put("jukebox_slots", slots);

        DelegatedIntSH activeSlotHandler = new DelegatedIntSH(slotUpdate.getPanel(), slotIndex, -1);
        slotUpdate.getSyncManager()
            .syncValue("jukebox_slot_active_" + slotIndex, activeSlotHandler);
        slotUpdate.put("jukebox_slot_active", activeSlotHandler);

        DelegatedBooleanSH isPlayingHandler = new DelegatedBooleanSH(slotUpdate.getPanel(), slotIndex);
        slotUpdate.getSyncManager()
            .syncValue("jukebox_is_playing_" + slotIndex, isPlayingHandler);
        slotUpdate.put("jukebox_is_playing", isPlayingHandler);
    }
}
