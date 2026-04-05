package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;

public class AdvancedJukeboxStorageSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {

        DelegatedStackHandlerSH handler = new DelegatedStackHandlerSH(group.wrapper, group.slotIndex, 16);

        group.syncManager.syncValue("adv_jukebox_delegation_" + group.slotIndex, handler);

        group.put("adv_jukebox_handler", handler);

        ModularSlot[] slots = new ModularSlot[16];
        for (int i = 0; i < 16; i++) {
            ModularSlot slot = new ModularSlot(handler.delegatedStackHandler, i);
            slot.slotGroup("adv_jukebox_records_" + group.slotIndex);
            group.syncManager.syncValue("adv_jukebox_handler_" + group.slotIndex, i, new ItemSlotSH(slot));
            slots[i] = slot;
        }

        group.put("adv_jukebox_record_slots", slots);
        group.syncManager.registerSlotGroup(new SlotGroup("adv_jukebox_records_" + group.slotIndex, 4, false));
    }
}
