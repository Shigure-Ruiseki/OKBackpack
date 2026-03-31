package ruiseki.okbackpack.common.block;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class BackpackItemStackHelpers {

    public static NBTTagCompound saveAllSlotsExtended(NBTTagCompound nbt, List<ItemStack> inventory) {
        NBTTagList list = new NBTTagList();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.get(i);

            if (stack != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("Slot", i);
                writeToNBTExtended(stack, tag);
                list.appendTag(tag);
            }
        }

        nbt.setTag("Items", list);
        return nbt;
    }

    public static NBTTagCompound writeToNBTExtended(ItemStack stack, NBTTagCompound nbt) {
        stack.writeToNBT(nbt);
        nbt.setInteger("Count", stack.stackSize);
        return nbt;
    }

    public static void loadAllItemsExtended(NBTTagCompound nbt, List<ItemStack> inventory) {
        NBTTagList list = nbt.getTagList("Items", 10);

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int j;

            if (tag.hasKey("Slot", 3)) {
                j = tag.getInteger("Slot"); // since 1.1.5
            } else if (tag.hasKey("Slot", 1)) {
                j = tag.getByte("Slot") & 255; // pre 1.1.5
            } else {
                j = 0; // fallback
            }

            if (j < inventory.size()) {
                inventory.set(j, loadItemStackExtended(tag));
            }
        }
    }

    public static ItemStack loadItemStackExtended(NBTTagCompound nbt) {
        ItemStack stack = ItemStack.loadItemStackFromNBT(nbt);
        if (stack != null) {
            stack.stackSize = nbt.getInteger("Count");
        }
        return stack;
    }
}
