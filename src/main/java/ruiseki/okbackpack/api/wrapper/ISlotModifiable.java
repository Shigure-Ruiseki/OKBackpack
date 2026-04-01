package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

public interface ISlotModifiable {

    default int modifySlotLimit(int original, int slot) {
        return original;
    }

    default int modifyStackLimit(int original, int slot, ItemStack stack) {
        return original;
    }
}
