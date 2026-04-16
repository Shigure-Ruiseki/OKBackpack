package ruiseki.okbackpack.client.gui.widget.updateGroup;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;

import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.api.wrapper.IArcaneCraftingUpgrade;
import ruiseki.okbackpack.api.wrapper.IUpgradeWrapper;
import ruiseki.okbackpack.client.gui.handler.IndexedInventoryCraftingWrapper;
import ruiseki.okbackpack.client.gui.slot.CraftingSlotInfo;
import ruiseki.okbackpack.client.gui.slot.IndexedModularArcaneSlot;
import ruiseki.okbackpack.client.gui.slot.IndexedModularCraftingMatrixSlot;
import ruiseki.okbackpack.client.gui.slot.ModularUpgradeWidgetSlot;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSH;
import ruiseki.okbackpack.client.gui.syncHandler.DelegatedStackHandlerSHRegisters;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okbackpack.compat.thaumcraft.ThaumcraftHelpers;
import ruiseki.okbackpack.compat.tic.TinkersHelpers;
import ruiseki.okcore.item.EmptyHandler;
import ruiseki.okcore.item.IItemHandler;
import thaumcraft.api.crafting.IArcaneRecipe;

public class ArcaneCraftingSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {
        IStoragePanel<?> panel = group.panel;

        DelegatedStackHandlerSH arcaneStackHandler = new DelegatedStackHandlerSH(
            group.panel::getContainer,
            group.wrapper,
            group.slotIndex,
            11) {

            @Override
            public void setDelegatedStackHandler(Supplier<IItemHandler> delegated) {
                super.setDelegatedStackHandler(delegated);
                updateInventoryCrafting(this, panel);
            }
        };
        group.syncManager.syncValue("arcane_delegation_" + group.slotIndex, arcaneStackHandler);
        group.put("arcane_handler", arcaneStackHandler);

        ModularSlot[] slots = new ModularSlot[9];
        for (int i = 0; i < 9; i++) {
            ModularSlot slot = new IndexedModularCraftingMatrixSlot(
                group.slotIndex,
                arcaneStackHandler.delegatedStackHandler,
                i);
            slot.slotGroup("arcane_result_" + group.slotIndex);
            group.syncManager.syncValue("arcane_slot_" + group.slotIndex, i, new ItemSlotSH(slot));
            slots[i] = slot;

            slot.changeListener((stack, onlyAmountChanged, client, init) -> {
                if (!client) return;
                DelegatedStackHandlerSH handler = group.get("arcane_handler");
                handler.syncToServer(
                    DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_ARCANE_CRAFTING_CHANGES));
            });
        }
        group.put("arcane_matrix_slots", slots);

        group.syncManager.registerSlotGroup(new SlotGroup("arcane_matrix_" + group.slotIndex, 3, false));

        IndexedModularArcaneSlot arcaneOutputSlot = new IndexedModularArcaneSlot(
            group.slotIndex,
            group.wrapper,
            arcaneStackHandler.delegatedStackHandler,
            9);
        arcaneOutputSlot.slotGroup("arcane_result_" + group.slotIndex);
        group.syncManager.syncValue("arcane_result_" + group.slotIndex, 0, new ItemSlotSH(arcaneOutputSlot));
        group.put("arcane_result_slot", arcaneOutputSlot);

        group.syncManager.registerSlotGroup(new SlotGroup("arcane_result_" + group.slotIndex, 1, false));

        ModularUpgradeWidgetSlot wandSlot = new ModularUpgradeWidgetSlot(
            group.slotIndex,
            arcaneStackHandler.delegatedStackHandler,
            IArcaneCraftingUpgrade.WAND_SLOT_INDEX) {

            @Override
            public boolean canShiftClickInsert(ItemStack stack) {
                return ThaumcraftHelpers.isWand(stack);
            }
        };
        wandSlot.filter(stack -> Mods.Thaumcraft.isLoaded() && ThaumcraftHelpers.isWand(stack));
        wandSlot.slotGroup("arcane_wand_" + group.slotIndex);
        group.syncManager.syncValue("arcane_wand_" + group.slotIndex, 0, new ItemSlotSH(wandSlot));
        group.put("arcane_wand_slot", wandSlot);

        group.syncManager.registerSlotGroup(new SlotGroup("arcane_wand_" + group.slotIndex, 1, false));

        wandSlot.changeListener((stack, onlyAmountChanged, client, init) -> {
            if (!client) return;
            DelegatedStackHandlerSH handler = group.get("arcane_handler");
            handler.syncToServer(
                DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_ARCANE_CRAFTING_CHANGES));
        });

        CraftingSlotInfo craftingInfo = new CraftingSlotInfo(slots, arcaneOutputSlot);
        group.put("arcane_info", craftingInfo);
    }

    public void updateInventoryCrafting(DelegatedStackHandlerSH handler, IStoragePanel<?> panel) {
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
        if (!(wrapper instanceof IArcaneCraftingUpgrade arcaneWrapper)) {
            return;
        }

        inventoryCrafting.setCraftingDestination(arcaneWrapper.getCraftingDes());

        if (handler.isValid() && !handler.getSyncManager()
            .isClient() && !(handler.delegatedStackHandler.get() instanceof EmptyHandler)) {
            findAndSyncResult(handler, inventoryCrafting, arcaneWrapper, panel);
        }
    }

    private void findAndSyncResult(DelegatedStackHandlerSH handler, IndexedInventoryCraftingWrapper inventoryCrafting,
        IArcaneCraftingUpgrade arcaneWrapper, IStoragePanel<?> panel) {

        EntityPlayer player = panel.getPlayer();
        int resultSlot = inventoryCrafting.getSizeInventory() - 1;

        ItemStack wandStack = handler.delegatedStackHandler.get()
            .getStackInSlot(IArcaneCraftingUpgrade.WAND_SLOT_INDEX);
        boolean hasWand = Mods.Thaumcraft.isLoaded() && ThaumcraftHelpers.isWand(wandStack);

        if (hasWand && Mods.Thaumcraft.isLoaded()) {
            IArcaneRecipe recipe = ThaumcraftHelpers.findArcaneRecipeIgnoringResearch(inventoryCrafting, player);
            if (recipe != null) {
                String researchKey = recipe.getResearch();
                boolean researchDone = researchKey == null || researchKey.isEmpty()
                    || ThaumcraftHelpers.isResearchComplete(player, researchKey);
                Map<String, Integer> aspects = ThaumcraftHelpers.getArcaneRecipeAspects(recipe);

                if (researchDone) {
                    ItemStack arcaneResult = recipe.getCraftingResult(inventoryCrafting);
                    handler.delegatedStackHandler.setStackInSlot(resultSlot, arcaneResult);
                    arcaneWrapper.setRequiredAspects(aspects);
                    arcaneWrapper.setMissingResearch(null);
                    arcaneWrapper.setMissingResearchName(null);

                    handler.syncToClient(
                        DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_ARCANE_CRAFTING),
                        buffer -> {
                            buffer.writeBoolean(hasWand);
                            buffer.writeItemStackToBuffer(arcaneResult);
                            ThaumcraftHelpers.writeAspectMap(buffer, aspects);
                            buffer.writeBoolean(false);
                        });
                } else {
                    String researchName = ThaumcraftHelpers.getResearchDisplayName(researchKey);

                    handler.delegatedStackHandler.setStackInSlot(resultSlot, null);
                    arcaneWrapper.setRequiredAspects(aspects);
                    arcaneWrapper.setMissingResearch(researchKey);
                    arcaneWrapper.setMissingResearchName(researchName);

                    handler.syncToClient(
                        DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_ARCANE_CRAFTING),
                        buffer -> {
                            buffer.writeBoolean(hasWand);
                            buffer.writeItemStackToBuffer(null);
                            ThaumcraftHelpers.writeAspectMap(buffer, aspects);
                            buffer.writeBoolean(true);
                            try {
                                buffer.writeStringToBuffer(researchKey);
                                buffer.writeStringToBuffer(researchName);
                            } catch (IOException ignored) {}
                        });
                }
                return;
            }
        }

        ItemStack standardResult;
        if (Mods.TConstruct.isLoaded()) {
            standardResult = TinkersHelpers.getTinkersRecipe(inventoryCrafting);
        } else {
            standardResult = CraftingManager.getInstance()
                .findMatchingRecipe(inventoryCrafting, player.worldObj);
        }
        handler.delegatedStackHandler.setStackInSlot(resultSlot, standardResult);

        arcaneWrapper.setRequiredAspects(null);
        arcaneWrapper.setMissingResearch(null);
        arcaneWrapper.setMissingResearchName(null);

        handler.syncToClient(
            DelegatedStackHandlerSH.getId(DelegatedStackHandlerSHRegisters.UPDATE_ARCANE_CRAFTING),
            buffer -> {
                buffer.writeBoolean(true);
                buffer.writeItemStackToBuffer(standardResult);
                buffer.writeInt(0);
                buffer.writeBoolean(false);
            });
    }
}
