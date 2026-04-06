package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.upgrade.UpgradeSlotChangeResult;

public interface ISlotModifiable {

    default boolean canAddStack(int slot, ItemStack stack) {
        return true;
    }

    default boolean canAddUpgrade(int slot, ItemStack stack) {
        return true;
    }

    default boolean canRemoveUpgrade(int slotIndex) {
        return true;
    }

    default UpgradeSlotChangeResult getRemoveUpgradeResult(int slotIndex) {
        return canRemoveUpgrade(slotIndex) ? UpgradeSlotChangeResult.success()
            : UpgradeSlotChangeResult.fail("", new int[0]);
    }

    default boolean canReplaceUpgrade(int slotIndex, ItemStack replacement) {
        return true;
    }

    default UpgradeSlotChangeResult getReplaceUpgradeResult(int slotIndex, ItemStack replacement) {
        return canReplaceUpgrade(slotIndex, replacement) ? UpgradeSlotChangeResult.success()
            : UpgradeSlotChangeResult.fail("", new int[0]);
    }
}
