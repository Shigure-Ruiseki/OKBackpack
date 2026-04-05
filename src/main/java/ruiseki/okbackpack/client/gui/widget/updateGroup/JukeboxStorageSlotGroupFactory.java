package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.api.wrapper.IStorageUpgrade;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;

public class JukeboxStorageSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {

        DelegatedStackHandlerSH handler = new DelegatedStackHandlerSH(group.wrapper, group.slotIndex, 1);

        IUpgradeWrapper upgradeWrapper = group.wrapper.getUpgradeHandler()
            .getWrapperInSlot(group.slotIndex);
        if (upgradeWrapper instanceof IStorageUpgrade storageUpgrade && storageUpgrade.getStorage()
            .getSlots() == 1) {
            handler.setDelegatedStackHandler(storageUpgrade::getStorage);
        }

        group.syncManager.syncValue("jukebox_delegation_" + group.slotIndex, handler);

        group.put("jukebox_handler", handler);

        ModularSlot[] slots = new ModularSlot[1];
        for (int i = 0; i < 1; i++) {
            ModularSlot slot = new ModularSlot(handler.delegatedStackHandler, i);
            slot.slotGroup("jukebox_records_" + group.slotIndex);
            group.syncManager.syncValue("jukebox_handler_" + group.slotIndex, i, new ItemSlotSH(slot));
            slots[i] = slot;
        }

        group.put("jukebox_record_slots", slots);
        group.syncManager.registerSlotGroup(new SlotGroup("jukebox_records_" + group.slotIndex, 1, false));
    }
}
