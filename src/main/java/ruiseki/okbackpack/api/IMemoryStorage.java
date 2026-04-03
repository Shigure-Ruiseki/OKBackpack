package ruiseki.okbackpack.api;

import net.minecraft.item.ItemStack;

public interface IMemoryStorage {

    boolean isSlotMemorized(int slotIndex);

    ItemStack getMemorizedStack(int slotIndex);

    void setMemoryStack(int slotIndex, boolean respectNBT);

    void unsetMemoryStack(int slotIndex);

    boolean isMemoryStackRespectNBT(int slotIndex);

    void setMemoryStackRespectNBT(int slotIndex, boolean respectNBT);

    IMemoryItemHandler getMemoryItemHandler();
}
