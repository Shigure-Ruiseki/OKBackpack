package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

public interface ISlotModifiable {

    default int modifySlotLimit(int original, int slot) {
        return original;
    }

    default int modifyStackLimit(int original, int slot, ItemStack stack) {
        return original;
    }

    default boolean canAddStack(int slot, ItemStack stack) {
        return true;
    }

    default boolean canAddUpgrade(int slot, ItemStack stack) {
        return true;
    }

    default boolean canRemoveUpgrade(int slotIndex) {
        return true;
    }

    default boolean canReplaceUpgrade(int slotIndex, ItemStack replacement) {
        return true;
    }
}
