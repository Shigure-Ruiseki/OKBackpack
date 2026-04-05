package ruiseki.okbackpack.common.item.wrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.cleanroommc.modularui.utils.item.ItemHandlerHelper;
import com.gtnewhorizon.gtnhlib.util.data.ItemId;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.ICompactingUpgrade;
import ruiseki.okbackpack.client.gui.handler.BackpackItemStackHandler;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.recipe.CompactingRecipeCache;
import ruiseki.okbackpack.common.recipe.CompactingRecipeCache.CompactingResult;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.ItemNBTHelpers;

public class AdvancedCompactingUpgradeWrapper extends AdvancedUpgradeWrapper implements ICompactingUpgrade {

    public AdvancedCompactingUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
    }

    @Override
    public String getSettingLangKey() {
        return "gui.backpack.advanced_compacting_settings";
    }

    @Override
    public boolean allowsGrid3x3() {
        return true;
    }

    @Override
    public boolean isOnlyReversible() {
        return ItemNBTHelpers.getBoolean(upgrade, ONLY_REVERSIBLE_TAG, true);
    }

    @Override
    public void setOnlyReversible(boolean onlyReversible) {
        ItemNBTHelpers.setBoolean(upgrade, ONLY_REVERSIBLE_TAG, onlyReversible);
    }

    @Override
    public void compactInventory() {
        doCompact();
    }

    private void doCompact() {
        if (!isEnabled()) return;
        if (!(storage instanceof BackpackWrapper bw)) return;

        BackpackItemStackHandler invHandler = bw.backpackHandler;
        CompactingRecipeCache cache = CompactingRecipeCache.getInstance();
        boolean onlyReversible = isOnlyReversible();

        Map<ItemId, List<Integer>> stackSlots = new HashMap<>();

        for (int i = 0; i < invHandler.getSlots(); i++) {
            ItemStack stack = invHandler.getStackInSlot(i);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!checkFilter(stack)) continue;

            ItemId id = ItemId.create(stack);
            stackSlots.computeIfAbsent(id, k -> new ArrayList<>())
                .add(i);
        }

        for (Map.Entry<ItemId, List<Integer>> entry : stackSlots.entrySet()) {
            List<Integer> slots = entry.getValue();
            if (slots.isEmpty()) continue;

            ItemStack template = invHandler.getStackInSlot(slots.get(0));
            CompactingResult result = cache.findCompactingRecipe(template, true, onlyReversible);
            if (result == null) continue;

            int inputCount = result.inputCount();

            // Sum total count of this item type across all slots
            int totalCount = 0;
            for (int idx : slots) {
                ItemStack stack = invHandler.getStackInSlot(idx);
                if (stack == null || stack.stackSize <= 0) continue;
                if (!ItemHandlerHelper.canItemStacksStack(template, stack)) continue;
                totalCount += stack.stackSize;
            }

            if (totalCount < inputCount) continue;

            int compactableUnits = totalCount / inputCount;

            ItemStack outputCopy = result.output()
                .copy();
            outputCopy.stackSize = compactableUnits;

            ItemStack remaining = tryInsertOutput(outputCopy, invHandler);

            int insertedUnits = compactableUnits - (remaining == null ? 0 : remaining.stackSize);
            if (insertedUnits <= 0) continue;

            int consumed = insertedUnits * inputCount;
            consumeFromSlotsBySlots(invHandler, slots, consumed);
        }
    }

    private void consumeFromSlotsBySlots(BackpackItemStackHandler invHandler, List<Integer> slots, int amount) {
        for (int idx : slots) {
            if (amount <= 0) break;
            ItemStack stack = invHandler.getStackInSlot(idx);
            if (stack == null || stack.stackSize <= 0) continue;

            int take = Math.min(stack.stackSize, amount);
            stack.stackSize -= take;
            amount -= take;

            if (stack.stackSize <= 0) {
                invHandler.setStackInSlot(idx, null);
            } else {
                invHandler.setStackInSlot(idx, stack);
            }
        }
    }

    private ItemStack tryInsertOutput(ItemStack output, BackpackItemStackHandler invHandler) {
        if (output == null) return null;

        ItemStack remaining = ItemHandlerHelper.copyStackWithSize(output, output.stackSize);

        for (int i = 0; i < invHandler.getSlots() && remaining != null; i++) {
            ItemStack existing = invHandler.getStackInSlot(i);
            if (existing == null) continue;
            if (!ItemHandlerHelper.canItemStacksStack(existing, remaining)) continue;

            int limit = invHandler.getSlotLimit(i);
            int space = limit - existing.stackSize;
            if (space <= 0) continue;

            int toInsert = Math.min(space, remaining.stackSize);
            existing.stackSize += toInsert;
            invHandler.setStackInSlot(i, existing);

            remaining.stackSize -= toInsert;
            if (remaining.stackSize <= 0) return null;
        }

        for (int i = 0; i < invHandler.getSlots() && remaining != null; i++) {
            ItemStack existing = invHandler.getStackInSlot(i);
            if (existing != null) continue;

            int limit = invHandler.getSlotLimit(i);
            int toInsert = Math.min(limit, remaining.stackSize);

            ItemStack placed = remaining.copy();
            placed.stackSize = toInsert;
            invHandler.setStackInSlot(i, placed);

            remaining.stackSize -= toInsert;
            if (remaining.stackSize <= 0) return null;
        }

        return remaining;
    }

    @Override
    public boolean tick(EntityPlayer player) {
        if (player.worldObj.isRemote) return false;
        compactInventory();
        return false;
    }

    @Override
    public boolean tick(World world, BlockPos pos) {
        if (world.isRemote) return false;
        compactInventory();
        return false;
    }
}
