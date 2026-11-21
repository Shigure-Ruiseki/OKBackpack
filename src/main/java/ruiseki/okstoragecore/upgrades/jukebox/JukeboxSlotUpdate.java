
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

        // Basic jukebox
        String basicSlotGroup = "basic_jukebox_" + slotIndex;
        String basicSlotSyncKey = "basic_jukebox_slot_" + slotIndex;

        DelegatedItemHandlerSH basicJukeboxHandler = new DelegatedItemHandlerSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotIndex,
            1);

        slotUpdate.getSyncManager()
            .syncValue("basic_jukebox_delegation_" + slotIndex, basicJukeboxHandler);
        slotUpdate.put("basic_jukebox_handler", basicJukeboxHandler);

        IndexedModularSlot basicSlot = new IndexedModularSlot(
            basicJukeboxHandler.getItemHandler(),
            0,
            slotUpdate.getPanel(),
            slotIndex);
        basicSlot.slotGroup(basicSlotGroup);

        slotUpdate.getSyncManager()
            .syncValue(basicSlotSyncKey, 0, new ItemSlotSH(basicSlot));
        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup(basicSlotGroup, 1, false));

        // Advanced jukebox
        String advancedSlotGroup = "adv_jukebox_" + slotIndex;
        String advancedSlotSyncKey = "adv_jukebox_slot_" + slotIndex;

        DelegatedItemHandlerSH advancedJukeboxHandler = new DelegatedItemHandlerSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotIndex,
            16);

        slotUpdate.getSyncManager()
            .syncValue("adv_jukebox_delegation_" + slotIndex, advancedJukeboxHandler);
        slotUpdate.put("adv_jukebox_handler", advancedJukeboxHandler);

        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup(advancedSlotGroup, 16, false));

        IndexedModularSlot[] advancedSlots = new IndexedModularSlot[16];
        for (int i = 0; i < advancedSlots.length; i++) {
            IndexedModularSlot slot = new IndexedModularSlot(
                advancedJukeboxHandler.getItemHandler(),
                i,
                slotUpdate.getPanel(),
                slotIndex);
            slot.slotGroup(advancedSlotGroup);

            slotUpdate.getSyncManager()
                .syncValue(advancedSlotSyncKey, i, new ItemSlotSH(slot));
            advancedSlots[i] = slot;
        }
        slotUpdate.put("adv_jukebox_slots", advancedSlots);

        // Shared playback state
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
