package ruiseki.okbackpack.client.gui.interaction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.client.gui.slot.AnvilOutputModularSlot;
import ruiseki.okbackpack.client.gui.slot.IndexedModularArcaneSlot;
import ruiseki.okbackpack.client.gui.slot.IndexedModularCraftingSlot;
import ruiseki.okbackpack.common.block.BackpackWrapper;
import ruiseki.okbackpack.common.helpers.BackpackEntityHelper;
import ruiseki.okbackpack.config.ModConfig;
import ruiseki.okcore.helper.ItemHandlerHelpers;

public final class BackpackInventoryInteractionAnalyzer {

    private BackpackInventoryInteractionAnalyzer() {}

    public static BackpackInventoryInteractionResult analyze(EntityPlayer player, Slot slot, ItemStack cursorStack) {
        if (!ModConfig.enableBackpackInventoryInteraction) {
            return BackpackInventoryInteractionResult.NONE;
        }

        if (player == null || slot == null || cursorStack == null || cursorStack.stackSize <= 0) {
            return BackpackInventoryInteractionResult.NONE;
        }

        if (BackpackEntityHelper.isBackpackStack(cursorStack, false)) {
            return analyzeSlotIntoCarriedBackpack(player, slot, cursorStack);
        }

        ItemStack slotStack = slot.getStack();
        if (BackpackEntityHelper.isBackpackStack(slotStack, false)) {
            return analyzeCursorIntoSlottedBackpack(player, slot, cursorStack, slotStack);
        }

        return BackpackInventoryInteractionResult.NONE;
    }

    public static boolean canRenderMinusForOutput(CapacitySummary summary) {
        return summary != null && summary.insertedCount() == summary.sourceCount();
    }

    public static boolean canRenderMinusForNormalSlot(CapacitySummary summary) {
        return summary != null && summary.insertedCount() > 0;
    }

    public static BackpackInventoryInteractionResult.OverlayState resolveOverlayState(boolean hasMatchingStackSpace,
        int insertedCount) {
        if (hasMatchingStackSpace && insertedCount > 0) {
            return BackpackInventoryInteractionResult.OverlayState.GREEN;
        }
        return BackpackInventoryInteractionResult.OverlayState.YELLOW;
    }

    public static boolean isOutputSlot(Slot slot) {
        return isOutputSlot(slot, null);
    }

    public static boolean isOutputSlot(Slot slot, EntityPlayer player) {
        return slot instanceof SlotCrafting || slot instanceof SlotFurnace
            || slot instanceof IndexedModularCraftingSlot
            || slot instanceof IndexedModularArcaneSlot
            || slot instanceof AnvilOutputModularSlot
            || isExtractOnlySlot(slot, player);
    }

    private static BackpackInventoryInteractionResult analyzeSlotIntoCarriedBackpack(EntityPlayer player, Slot slot,
        ItemStack backpackStack) {
        if (!slot.getHasStack() || !slot.canTakeStack(player)) {
            return BackpackInventoryInteractionResult.NONE;
        }

        ItemStack slotStack = slot.getStack();
        BackpackWrapper wrapper = BackpackEntityHelper.getInteractionWrapper(player, backpackStack);
        if (wrapper == null || !wrapper.canPlayerAccess(player.getUniqueID())) {
            return BackpackInventoryInteractionResult.NONE;
        }

        CapacitySummary summary = simulateInsert(wrapper, slotStack);
        boolean outputSlot = isOutputSlot(slot, player);
        boolean hasMatchingStackSpace = hasMatchingStackSpace(wrapper, slotStack);

        return BackpackInventoryInteractionResult.of(
            BackpackInventoryInteractionKind.INSERT_SLOT_INTO_CARRIED_BACKPACK,
            BackpackInventoryInteractionResult.OverlaySymbol.MINUS,
            resolveOverlayState(hasMatchingStackSpace, summary.insertedCount()),
            outputSlot,
            outputSlot,
            summary.sourceCount(),
            summary.insertedCount(),
            backpackStack);
    }

    private static BackpackInventoryInteractionResult analyzeCursorIntoSlottedBackpack(EntityPlayer player, Slot slot,
        ItemStack cursorStack, ItemStack backpackStack) {
        BackpackWrapper wrapper = BackpackEntityHelper.getInteractionWrapper(player, backpackStack);
        if (wrapper == null || !wrapper.canPlayerAccess(player.getUniqueID())) {
            return BackpackInventoryInteractionResult.NONE;
        }

        CapacitySummary summary = simulateInsert(wrapper, cursorStack);
        boolean hasMatchingStackSpace = hasMatchingStackSpace(wrapper, cursorStack);

        return BackpackInventoryInteractionResult.of(
            BackpackInventoryInteractionKind.INSERT_CURSOR_INTO_SLOTTED_BACKPACK,
            BackpackInventoryInteractionResult.OverlaySymbol.PLUS,
            resolveOverlayState(hasMatchingStackSpace, summary.insertedCount()),
            false,
            false,
            summary.sourceCount(),
            summary.insertedCount(),
            backpackStack);
    }

    private static CapacitySummary simulateInsert(BackpackWrapper wrapper, ItemStack sourceStack) {
        if (wrapper == null || sourceStack == null || sourceStack.stackSize <= 0) {
            return CapacitySummary.of(0, 0);
        }

        ItemStack simulatedSource = sourceStack.copy();
        ItemStack remaining = wrapper.insertItem(simulatedSource, true);
        int sourceCount = sourceStack.stackSize;
        int remainingCount = remaining == null ? 0 : Math.max(0, remaining.stackSize);
        int insertedCount = Math.max(0, sourceCount - remainingCount);
        return CapacitySummary.of(sourceCount, insertedCount);
    }

    private static boolean hasMatchingStackSpace(BackpackWrapper wrapper, ItemStack sourceStack) {
        if (wrapper == null || sourceStack == null) {
            return false;
        }

        for (int slot = 0; slot < wrapper.getStackHandler()
            .getSlots(); slot++) {
            ItemStack stackInSlot = wrapper.getStackHandler()
                .getStackInSlot(slot);
            if (stackInSlot == null || !ItemHandlerHelpers.canItemStacksStack(stackInSlot, sourceStack)) {
                continue;
            }

            int stackLimit = wrapper.getStackHandler()
                .getStackLimit(slot, stackInSlot);
            if (stackInSlot.stackSize < stackLimit) {
                return true;
            }
        }
        return false;
    }

    private static boolean isExtractOnlySlot(Slot slot, EntityPlayer player) {
        ItemStack slotStack = slot == null ? null : slot.getStack();
        return slotStack != null && (player == null || slot.canTakeStack(player)) && !slot.isItemValid(slotStack);
    }

    public static final class CapacitySummary {

        private final int sourceCount;
        private final int insertedCount;

        private CapacitySummary(int sourceCount, int insertedCount) {
            this.sourceCount = sourceCount;
            this.insertedCount = insertedCount;
        }

        public static CapacitySummary of(int sourceCount, int insertedCount) {
            return new CapacitySummary(sourceCount, insertedCount);
        }

        public int sourceCount() {
            return sourceCount;
        }

        public int insertedCount() {
            return insertedCount;
        }
    }
}
