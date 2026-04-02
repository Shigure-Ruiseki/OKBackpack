package ruiseki.okbackpack.api.wrapper;

import net.minecraft.item.ItemStack;

public interface IStackSizeUpgrade extends ISlotModifiable {

    int getMultiplier(ItemStack stack);

}
