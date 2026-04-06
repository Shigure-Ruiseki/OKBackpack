package ruiseki.okbackpack.client.gui.widget.updateGroup;

import java.util.function.Supplier;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.utils.item.EmptyHandler;
import com.cleanroommc.modularui.utils.item.IItemHandler;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularCraftingSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.api.wrapper.ICraftingUpgrade;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.handler.IndexedInventoryCraftingWrapper;
import ruiseki.okbackpack.client.gui.slot.CraftingSlotInfo;
import ruiseki.okbackpack.client.gui.slot.IndexedModularCraftingMatrixSlot;
import ruiseki.okbackpack.client.gui.slot.IndexedModularCraftingSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSHRegisters;

public class CraftingSlotGroup implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {

        DelegatedStackHandlerSH craftingStackHandler = new DelegatedStackHandlerSH(
            group.panel::getContainer,
            group.wrapper,
            group.slotIndex,
            10) {

            @Override
            public void setDelegatedStackHandler(Supplier<IItemHandler> delegated) {
                super.setDelegatedStackHandler(delegated);
                updateInventoryCrafting(this);
            }
        };
        group.syncManager.syncValue("crafting_delegation_" + group.slotIndex, craftingStackHandler);
        group.put("crafting_handler", craftingStackHandler);

        ModularSlot[] slots = new ModularSlot[9];
        for (int i = 0; i < 9; i++) {
            ModularSlot slot = new IndexedModularCraftingMatrixSlot(
                group.slotIndex,
                craftingStackHandler.delegatedStackHandler,
                i);
            slot.slotGroup("crafting_result_" + group.slotIndex);
            group.syncManager.syncValue("crafting_slot_" + group.slotIndex, i, new ItemSlotSH(slot));
            slots[i] = slot;

            slot.changeListener((stack, onlyAmountChanged, client, init) -> {
                if (!client) return;
                DelegatedStackHandlerSH handler = group.get("crafting_handler");
                handler.syncToServer(
                    DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_CRAFTING_CHANGES));
            });
        }
        group.put("crafting_matrix_slots", slots);

        group.syncManager.registerSlotGroup(new SlotGroup("crafting_matrix_" + group.slotIndex, 3, false));

        ModularCraftingSlot craftingOutputSlot = new IndexedModularCraftingSlot(
            group.slotIndex,
            group.wrapper,
            craftingStackHandler.delegatedStackHandler,
            9);
        craftingOutputSlot.slotGroup("crafting_result_" + group.slotIndex);
        group.syncManager.syncValue("crafting_result_" + group.slotIndex, 0, new ItemSlotSH(craftingOutputSlot));
        group.put("crafting_result_slot", craftingOutputSlot);

        group.syncManager.registerSlotGroup(new SlotGroup("crafting_result_" + group.slotIndex, 1, false));

        CraftingSlotInfo craftingInfo = new CraftingSlotInfo(slots, craftingOutputSlot);
        group.put("crafting_info", craftingInfo);

    }

    public void updateInventoryCrafting(DelegatedStackHandlerSH handler) {

        IndexedInventoryCraftingWrapper inventoryCrafting;
        if (handler.getInventory() instanceof IndexedInventoryCraftingWrapper inv) {
            inventoryCrafting = inv;
        } else {
            inventoryCrafting = new IndexedInventoryCraftingWrapper(
                handler.slotIndex,
                handler.containerProvider.get()
                    .getContainer(),
                3,
                3,
                handler.delegatedStackHandler,
                0);

            handler.setInventory(inventoryCrafting);

            handler.containerProvider.get()
                .registerInventoryCrafting(handler.slotIndex, inventoryCrafting);
        }

        IUpgradeWrapper wrapper = handler.wrapper.getUpgradeHandler()
            .getWrapperInSlot(handler.slotIndex);
        if (!(wrapper instanceof ICraftingUpgrade craftingWrapper)) {
            return;
        }

        inventoryCrafting.setCraftingDestination(craftingWrapper.getCraftingDes());

        inventoryCrafting.detectChanges();

        if (handler.isValid() && !handler.getSyncManager()
            .isClient() && !(handler.delegatedStackHandler.get() instanceof EmptyHandler)) {
            int resultSlot = inventoryCrafting.getSizeInventory() - 1;

            ItemStack result = handler.delegatedStackHandler.get()
                .getStackInSlot(resultSlot);

            handler.syncToClient(
                DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_CRAFTING),
                buffer -> buffer.writeItemStackToBuffer(result));
        }
    }
}
