package ruiseki.okbackpack.common.item.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStorageWrapper;
import ruiseki.okbackpack.api.wrapper.IStackSizeUpgrade;
import ruiseki.okbackpack.common.item.ItemStackUpgrade;

public class StackUpgradeWrapper extends UpgradeWrapperBase implements IStackSizeUpgrade {

    public StackUpgradeWrapper(ItemStack upgrade, IStorageWrapper storage) {
        super(upgrade, storage);
    }

    @Override
    public int modifySlotLimit(int original, int slot) {
        return original + getMultiplier(upgrade);
    }

    @Override
    public int modifyStackLimit(int original, int slot, ItemStack stack) {
        return original + getMultiplier(upgrade);
    }

    @Override
    public boolean canAddUpgrade(int slot, ItemStack stack) {
        return true; // luôn có thể thêm
    }

    @Override
    public boolean canRemoveUpgrade(int slotIndex) {

        int totalMultiplier = calculateMultiplierExcluding(slotIndex);

        for (ItemStack stack : storage.getStacks()) {
            if (stack == null) continue;

            int newLimit = stack.getMaxStackSize() * totalMultiplier;

            if (stack.stackSize > newLimit) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean canReplaceUpgrade(int slotIndex, ItemStack replacement) {
        if (replacement == null) return true;

        int totalOtherMultiplier = calculateMultiplierExcluding(slotIndex);

        UpgradeWrapperBase wrapper = UpgradeWrapperFactory.createWrapper(replacement, storage);

        int totalMultiplier = totalOtherMultiplier;

        if (wrapper instanceof IStackSizeUpgrade sizeUpgrade) {
            totalMultiplier += sizeUpgrade.getMultiplier(replacement);
        }

        for (ItemStack stack : storage.getStacks()) {
            if (stack == null) continue;

            int maxAllowed = stack.getMaxStackSize() * totalMultiplier;

            if (stack.stackSize > maxAllowed) {
                return false;
            }
        }

        return true;
    }

    private int calculateMultiplierExcluding(int excludedSlot) {
        int total = 0;

        for (var entry : storage.gatherCapabilityUpgrades(IStackSizeUpgrade.class)
            .entrySet()) {
            if (entry.getKey() == excludedSlot) continue;

            ItemStack stack = storage.getUpgradeHandler()
                .getStackInSlot(entry.getKey());
            if (stack == null) continue;

            total += entry.getValue()
                .getMultiplier(stack);
        }

        return total;
    }

    @Override
    public int getMultiplier(ItemStack stack) {
        return ItemStackUpgrade.multiplier(stack);
    }
}
