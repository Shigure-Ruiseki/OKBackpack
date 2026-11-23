package ruiseki.okbackpack.upgrades.anvil;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.slot.ModularResultSlot;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedStringSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class AnvilSlotUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate slotUpdate) {
        int slotIndex = slotUpdate.getUpgradeIndex();

        String inputGroupName = "anvil_slot_" + slotIndex;
        String resultGroupName = "anvil_result_" + slotIndex;

        DelegatedStringSH nameField = new DelegatedStringSH(slotUpdate.getPanel(), slotIndex);
        slotUpdate.getSyncManager()
            .syncValue("anvil_name_field_" + slotIndex, nameField);
        slotUpdate.put("anvil_name_field", nameField);

        DelegatedStringSH requiredField = new DelegatedStringSH(slotUpdate.getPanel(), slotIndex);
        slotUpdate.getSyncManager()
            .syncValue("anvil_required_field_" + slotIndex, requiredField);
        slotUpdate.put("anvil_required_field", requiredField);

        DelegatedItemHandlerSH anvilHandler = new DelegatedItemHandlerSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotIndex,
            2);

        slotUpdate.getSyncManager()
            .syncValue("anvil_delegation_" + slotIndex, anvilHandler);

        slotUpdate.put("anvil_handler", anvilHandler);

        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup(inputGroupName, 2, false));

        slotUpdate.getSyncManager()
            .registerSlotGroup(new SlotGroup(resultGroupName, 1, false));

        for (int i = 0; i < 2; i++) {
            IndexedModularSlot slot = new IndexedModularSlot(
                anvilHandler.getItemHandler(),
                i,
                slotUpdate.getPanel(),
                slotIndex);

            slot.changeListener((newItem, onlyAmountChanged, client, init) -> {
                if (slot.getSlotIndex() == 0) {
                    if (ItemHelpers.isEmpty(newItem)) {
                        nameField.setValue("");
                    } else {
                        nameField.setValue(newItem.getDisplayName());
                    }
                }
            });

            slot.slotGroup(inputGroupName);

            slotUpdate.getSyncManager()
                .syncValue("anvil_slot_" + slotIndex, i, new ItemSlotSH(slot));

            slotUpdate.put("anvil_slot_" + i, slot);
        }

        ModularResultSlot resultSlot = new ModularResultSlot(slotUpdate.getPanel(), slotIndex);

        resultSlot.slotGroup(resultGroupName);

        slotUpdate.getSyncManager()
            .syncValue(resultGroupName, 0, new ItemSlotSH(resultSlot));

        slotUpdate.put("anvil_result_slot", resultSlot);
    }
}
