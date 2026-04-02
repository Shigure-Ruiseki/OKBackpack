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
        // NO OP
        return true;
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

        if (!(replacement.getItem() instanceof IStackSizeUpgrade)) return true;

        int totalMultiplier = getMultiplier(replacement);

        for (var entry : storage.gatherCapabilityUpgrades(IStackSizeUpgrade.class)
            .entrySet()) {

            if (entry.getKey() == slotIndex) continue;

            IStackSizeUpgrade up = entry.getValue();

            ItemStack stack = storage.getUpgradeHandler()
                .getStackInSlot(entry.getKey());

            if (stack == null) continue;

            totalMultiplier += up.getMultiplier(stack);
        }

        for (ItemStack stack : storage.getStacks()) {
            if (stack == null) continue;

            int newLimit = stack.getMaxStackSize() + totalMultiplier;

            if (stack.stackSize > newLimit) {
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
