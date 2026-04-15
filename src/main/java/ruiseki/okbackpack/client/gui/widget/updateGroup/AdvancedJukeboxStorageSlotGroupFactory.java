package ruiseki.okbackpack.client.gui.widget.updateGroup;

import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.client.gui.slot.ModularUpgradeWidgetSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;

public class AdvancedJukeboxStorageSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {

        DelegatedStackHandlerSH handler = new DelegatedStackHandlerSH(
            group.panel::getContainer,
            group.wrapper,
            group.slotIndex,
            16);

        group.syncManager.syncValue("adv_jukebox_delegation_" + group.slotIndex, handler);

        group.put("adv_jukebox_handler", handler);

        ModularUpgradeWidgetSlot[] slots = new ModularUpgradeWidgetSlot[16];
        for (int i = 0; i < 16; i++) {
            ModularUpgradeWidgetSlot slot = new ModularUpgradeWidgetSlot(
                group.slotIndex,
                handler.delegatedStackHandler,
                i) {

                @Override
                public boolean canShiftClickInsert(ItemStack stack) {
                    return stack != null && stack.getItem() instanceof ItemRecord;
                }
            };
            slot.slotGroup("adv_jukebox_records_" + group.slotIndex);
            group.syncManager.syncValue("adv_jukebox_handler_" + group.slotIndex, i, new ItemSlotSH(slot));
            slots[i] = slot;
        }

        group.put("adv_jukebox_record_slots", slots);
        group.syncManager.registerSlotGroup(new SlotGroup("adv_jukebox_records_" + group.slotIndex, 4, false));
    }
}
