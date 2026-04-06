package ruiseki.okbackpack.common.item.stack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;
import ruiseki.okbackpack.api.wrapper.IInfinityUpgrade;
import ruiseki.okbackpack.api.wrapper.IStackSizeUpgrade;
import ruiseki.okbackpack.common.item.UpgradeWrapperBase;

public class StackUpgradeWrapper extends UpgradeWrapperBase implements IStackSizeUpgrade {

    public StackUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage, Consumer<ItemStack> upgradeConsumer) {
        super(upgrade, storage, upgradeConsumer);
    }

    @Override
    public boolean canAddUpgrade(int slot, ItemStack stack) {
        return true; // luôn có thể thêm
    }

    @Override
    public boolean canRemoveUpgrade(int slotIndex) {
        return getRemoveUpgradeResult(slotIndex).isSuccessful();
    }

    @Override
    public UpgradeSlotChangeResult getRemoveUpgradeResult(int slotIndex) {

        if (!storage.gatherCapabilityUpgrades(IInfinityUpgrade.class)
            .isEmpty()) {
            return UpgradeSlotChangeResult.success();
        }

        double totalMultiplier = Math.max(calculateMultiplierExcluding(slotIndex), 1.0);

        List<Integer> conflictSlots = new ArrayList<>();
        for (int i = 0; i < storage.getSlots(); i++) {
            ItemStack stack = storage.getStackInSlot(i);
            if (stack == null) continue;

            double rawLimit = stack.getMaxStackSize() * totalMultiplier;
            long newLimit = rawLimit >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (long) Math.ceil(rawLimit);

            if (stack.stackSize > newLimit) {
                conflictSlots.add(i);
            }
        }

        if (!conflictSlots.isEmpty()) {
            return UpgradeSlotChangeResult.failWithInventoryConflicts(
                "gui.backpack.error.remove.stack_low_multiplier",
                conflictSlots.stream()
                    .mapToInt(Integer::intValue)
                    .toArray(),
                ItemStackUpgrade.formatMultiplier(totalMultiplier));
        }

        return UpgradeSlotChangeResult.success();
    }

    @Override
    public boolean canReplaceUpgrade(int slotIndex, ItemStack replacement) {
        return getReplaceUpgradeResult(slotIndex, replacement).isSuccessful();
    }

    @Override
    public UpgradeSlotChangeResult getReplaceUpgradeResult(int slotIndex, ItemStack replacement) {
        if (replacement == null) return UpgradeSlotChangeResult.success();

        if (!storage.gatherCapabilityUpgrades(IInfinityUpgrade.class)
            .isEmpty()) {
            return UpgradeSlotChangeResult.success();
        }

        double totalMultiplier = calculateMultiplierExcluding(slotIndex);

        // Add the replacement's multiplier, not the current upgrade's
        if (replacement.getItem() instanceof ItemStackUpgrade) {
            totalMultiplier += ItemStackUpgrade.multiplier(replacement);
        }

        totalMultiplier = Math.max(totalMultiplier, 1.0);

        List<Integer> conflictSlots = new ArrayList<>();
        for (int i = 0; i < storage.getSlots(); i++) {
            ItemStack stack = storage.getStackInSlot(i);
            if (stack == null) continue;

            double rawLimit = stack.getMaxStackSize() * totalMultiplier;
            long maxAllowed = rawLimit >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (long) Math.ceil(rawLimit);

            if (stack.stackSize > maxAllowed) {
                conflictSlots.add(i);
            }
        }

        if (!conflictSlots.isEmpty()) {
            return UpgradeSlotChangeResult.failWithInventoryConflicts(
                "gui.backpack.error.remove.stack_low_multiplier",
                conflictSlots.stream()
                    .mapToInt(Integer::intValue)
                    .toArray(),
                ItemStackUpgrade.formatMultiplier(totalMultiplier));
        }

        return UpgradeSlotChangeResult.success();
    }

    private double calculateMultiplierExcluding(int excludedSlot) {
        double total = 0;

        for (var entry : storage.gatherCapabilityUpgrades(IStackSizeUpgrade.class)
            .entrySet()) {
            if (entry.getKey() == excludedSlot) continue;

            ItemStack stack = storage.getUpgradeHandler()
                .getStackInSlot(entry.getKey());
            if (stack == null) continue;

            total += entry.getValue()
                .getMultiplier();
        }

        return total;
    }

    @Override
    public double getMultiplier() {
        return ItemStackUpgrade.multiplier(upgrade);
    }
}
