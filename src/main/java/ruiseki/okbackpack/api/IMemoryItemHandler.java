package ruiseki.okbackpack.api;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IMemoryItemHandler {

    boolean isMemory(int slot);

    ItemStack getMemoryStack(int slot);

    void setMemoryStack(int slot, ItemStack stack);

    void clearMemoryStacks(int slot);

    boolean isRespectNBT(int slot);

    void setRespectNBT(int slot, boolean respect);

    ItemStack insertItemToMemorySlots(ItemStack stack, boolean simulate);

    ItemStack prioritizedInsertion(int slot, ItemStack stack, boolean simulate);

    List<ItemStack> getMemorizedStacks();

    List<Boolean> getRespectNBTList();
}
