package ruiseki.okbackpack.common.helpers;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import ruiseki.okcore.helper.ItemHelpers;

public class BackpackItemStackHelpers {

    public static NBTTagCompound saveAllSlotsExtended(NBTTagCompound nbt, List<ItemStack> inventory) {
        NBTTagList list = new NBTTagList();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);

            if (stack != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("Slot", i);
                ItemHelpers.writeToNBT(stack, tag);
                list.appendTag(tag);
            }
        }

        nbt.setTag("Items", list);
        return nbt;
    }

    public static void loadAllItemsExtended(NBTTagCompound nbt, List<ItemStack> inventory) {
        NBTTagList list = nbt.getTagList("Items", 10);

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getInteger("Slot");
            if (slot < inventory.size()) {
                inventory.set(slot, ItemHelpers.readFromNBT(nbt));
            }
        }
    }
}
