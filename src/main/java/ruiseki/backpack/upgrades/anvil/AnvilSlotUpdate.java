package ruiseki.backpack.upgrades.anvil;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.backpack.client.gui.slot.AnvilResultSlot;
import ruiseki.okstoragecore.client.gui.slot.IndexedModularSlot;
import ruiseki.okstoragecore.client.gui.sync.DelegatedItemHandlerSH;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class AnvilSlotUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate group) {
        int slotIndex = group.getUpgradeIndex();

        String inputGroupName = "anvil_slot_" + slotIndex;
        String resultGroupName = "anvil_result_" + slotIndex;

        DelegatedItemHandlerSH anvilHandler = new DelegatedItemHandlerSH(
            group.getPanel(),
            group::getContainer,
            slotIndex,
            2);

        group.getSyncManager()
            .syncValue("anvil_delegation_" + slotIndex, anvilHandler);

        group.put("anvil_handler", anvilHandler);

        group.getSyncManager()
            .registerSlotGroup(new SlotGroup(inputGroupName, 2, false));

        group.getSyncManager()
            .registerSlotGroup(new SlotGroup(resultGroupName, 1, false));

        for (int i = 0; i < 2; i++) {
            IndexedModularSlot slot = new IndexedModularSlot(
                anvilHandler.getItemHandler(),
                i,
                group.getPanel(),
                slotIndex);

            slot.slotGroup(inputGroupName);

            group.getSyncManager()
                .syncValue("anvil_slot_" + slotIndex, i, new ItemSlotSH(slot));

            group.put("anvil_slot_" + i, slot);
        }

        AnvilResultSlot resultSlot = new AnvilResultSlot(group.getPanel(), slotIndex);

        resultSlot.slotGroup(resultGroupName);

        group.getSyncManager()
            .syncValue(resultGroupName, 0, new ItemSlotSH(resultSlot));

        group.put("anvil_result_slot", resultSlot);

        AnvilContainer container = new AnvilContainer(
            group.getWrapper(),
            group.getPanel(),
            anvilHandler.getContainerSupplier(),
            group.getPlayer(),
            slotIndex,
            anvilHandler.getItemHandler());

        resultSlot.setAnvilContainer(container);

        group.setUpgradeContainer(container);
    }
}
