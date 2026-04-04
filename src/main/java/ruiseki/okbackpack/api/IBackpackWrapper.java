package ruiseki.okbackpack.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.factory.inventory.InventoryType;

public interface IBackpackWrapper extends IStorageWrapper {

    IBackpackWrapper setBackpackStack(ItemStack backpackStack);

    ItemStack getBackpack();

    NBTTagCompound getBackpackNBT();

    void writeToItem();

    void readFromItem();

    void writeToItem(EntityPlayer player);

    void syncToServer();

    boolean tick(EntityPlayer player);

    InventoryType getType();

    void setType(InventoryType type);

    int getSlotIndex();

    void setSlotIndex(int slotIndex);
}
