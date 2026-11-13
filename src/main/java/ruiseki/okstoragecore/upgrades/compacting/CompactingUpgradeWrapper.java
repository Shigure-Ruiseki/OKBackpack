
package ruiseki.okstoragecore.upgrades.compacting;

import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.jetbrains.annotations.Nullable;

import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.item.handler.IItemHandlerSimpleInserter;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.helper.RecipeHelpers;
import ruiseki.okstoragecore.helper.RecipeHelpers.CompactingResult;
import ruiseki.okstoragecore.helper.RecipeHelpers.CompactingShape;
import ruiseki.okstoragecore.upgrades.FilterLogic;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.ITickableUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class CompactingUpgradeWrapper extends UpgradeWrapperBase<CompactingUpgradeWrapper, ItemCompactingUpgrade>
    implements IFilteredUpgrade, ITickableUpgrade {

    private static final int COOLDOWN_TICKS = 5;
    private static final int FULL_COOLDOWN_TICKS = 20;

    private final FilterLogic filterLogic;
    private final int slotCount;

    protected CompactingUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {

        super(storageWrapper, upgrade, upgradeSaveHandler);
        this.slotCount = upgradeItem.getFilterSlotCount();
        filterLogic = new FilterLogic(upgrade, upgradeSaveHandler, slotCount, this::canCompact);
    }

    public int getSlotCount() {
        return slotCount;
    }

    private boolean canCompact(ItemStack stack) {
        if (ItemHelpers.isEmpty(stack)) return false;
        return getCompactingShape(stack) != CompactingShape.NONE;
    }

    private CompactingShape getCompactingShape(ItemStack stack) {
        Set<CompactingShape> shapes = RecipeHelpers.getItemCompactingShapes(stack);

        boolean allowNonUncraftable = shouldCompactNonUncraftable();

        // Prefer 3x3 when enabled and available.
        if (upgradeItem.shouldCompactThreeByThree()) {
            if (shapes.contains(CompactingShape.THREE_BY_THREE_UNCRAFTABLE)
                || (allowNonUncraftable && shapes.contains(CompactingShape.THREE_BY_THREE))) {
                return shapes.contains(CompactingShape.THREE_BY_THREE_UNCRAFTABLE)
                    ? CompactingShape.THREE_BY_THREE_UNCRAFTABLE
                    : CompactingShape.THREE_BY_THREE;
            }
        }

        // Fall back to 2x2.
        if (shapes.contains(CompactingShape.TWO_BY_TWO_UNCRAFTABLE)
            || (allowNonUncraftable && shapes.contains(CompactingShape.TWO_BY_TWO))) {
            return shapes.contains(CompactingShape.TWO_BY_TWO_UNCRAFTABLE) ? CompactingShape.TWO_BY_TWO_UNCRAFTABLE
                : CompactingShape.TWO_BY_TWO;
        }

        return CompactingShape.NONE;
    }

    private boolean compactSlot(IItemHandlerSimpleInserter inventoryHandler, int slot) {
        ItemStack slotStack = inventoryHandler.getStackInSlot(slot);

        if (ItemHelpers.isEmpty(slotStack) || !filterLogic.matchesFilter(slotStack)) {
            return false;
        }

        CompactingShape shape = getCompactingShape(slotStack);
        if (shape == CompactingShape.NONE) {
            return false;
        }

        CompactingResult result = RecipeHelpers.getCompactingResult(slotStack, shape);
        ItemStack output = result.getResult();
        if (ItemHelpers.isEmpty(output)) {
            return false;
        }

        return compact(inventoryHandler, slotStack, shape.getNumberOfIngredients(), output);
    }

    private boolean compact(IItemHandlerSimpleInserter inventoryHandler, ItemStack inputStack, int inputCount,
        ItemStack outputStack) {

        ItemStack requiredInput = ItemHelpers.copy(inputStack);
        ItemHelpers.setCount(requiredInput, inputCount);

        boolean compacted = false;
        while (true) {
            ItemStack simulatedExtraction = InventoryHelpers
                .extractFromInventory(requiredInput, inventoryHandler, true);
            if (ItemHelpers.getCount(simulatedExtraction) < inputCount) {
                break;
            }

            ItemStack resultCopy = ItemHelpers.copy(outputStack);
            if (!ItemHelpers.isEmpty(InventoryHelpers.insertIntoInventory(resultCopy, inventoryHandler, true))) {
                break;
            }

            ItemStack extractedStack = InventoryHelpers.extractFromInventory(requiredInput, inventoryHandler, false);
            if (ItemHelpers.getCount(extractedStack) < inputCount) {
                break;
            }

            ItemStack remaining = InventoryHelpers.insertIntoInventory(resultCopy, inventoryHandler, false);
            if (!ItemHelpers.isEmpty(remaining)) {
                break;
            }

            compacted = true;
        }
        return compacted;
    }

    @Override
    public FilterLogic getFilterLogic() {
        return filterLogic;
    }

    public boolean shouldCompactNonUncraftable() {
        return NBTHelpers.getBoolean(upgrade, "compactNonUncraftable")
            .orElse(false);
    }

    public void setCompactNonUncraftable(boolean shouldCompactNonUncraftable) {
        NBTHelpers.setBoolean(upgrade, "compactNonUncraftable", shouldCompactNonUncraftable);
        save();
    }

    @Override
    public void tick(@Nullable EntityLivingBase entity, World world, BlockPos pos) {
        if (isInCooldown(world)) return;

        IItemHandlerSimpleInserter inventoryHandler = storageWrapper.getInventoryHandler();

        boolean compacted = false;
        for (int slot = 0; slot < inventoryHandler.getSlots(); slot++) {
            if (compactSlot(inventoryHandler, slot)) {
                compacted = true;
            }
        }

        setCooldown(world, compacted ? COOLDOWN_TICKS : FULL_COOLDOWN_TICKS);
    }
}
