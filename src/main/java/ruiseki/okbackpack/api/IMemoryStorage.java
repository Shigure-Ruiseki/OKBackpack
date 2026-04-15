package ruiseki.okbackpack.api;

import net.minecraft.item.ItemStack;

public interface IMemoryStorage {

    String MEMORY_STACK_ITEMS_TAG = "MemoryItems";
    String MEMORY_STACK_RESPECT_NBT_TAG = "MemoryRespectNBT";

    boolean isSlotMemorized(int slotIndex);

    ItemStack getMemoryStack(int slotIndex);

    void setMemoryStack(int slotIndex, boolean respectNBT);

    void unsetMemoryStack(int slotIndex);

    boolean isMemoryStackRespectNBT(int slotIndex);

    void setMemoryStackRespectNBT(int slotIndex, boolean respectNBT);
}
