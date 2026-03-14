package ruiseki.okbackpack.common.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.ShapedOreRecipe;

import ruiseki.okbackpack.common.block.BackpackWrapper;

public class BackpackDyeRecipe extends ShapedOreRecipe {

    private final int mainColor;
    private final int accentColor;

    public BackpackDyeRecipe(ItemStack result, int mainColor, int accentColor, Object... recipe) {
        super(result, recipe);
        this.mainColor = mainColor;
        this.accentColor = accentColor;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {

        ItemStack result = super.getCraftingResult(inv);
        if (result == null) return null;

        NBTTagCompound root = result.hasTagCompound() ? (NBTTagCompound) result.getTagCompound()
            .copy() : new NBTTagCompound();

        NBTTagCompound backpackNBT = root.hasKey(BackpackWrapper.BACKPACK_NBT)
            ? root.getCompoundTag(BackpackWrapper.BACKPACK_NBT)
            : new NBTTagCompound();

        // copy NBT từ input backpack
        for (int i = 0; i < inv.getSizeInventory(); i++) {

            ItemStack stack = inv.getStackInSlot(i);

            if (stack != null && stack.hasTagCompound()
                && stack.getTagCompound()
                    .hasKey(BackpackWrapper.BACKPACK_NBT)) {

                backpackNBT = (NBTTagCompound) stack.getTagCompound()
                    .getCompoundTag(BackpackWrapper.BACKPACK_NBT)
                    .copy();
                break;
            }
        }

        // set màu vào BackpackNBT
        if (mainColor >= 0) {
            backpackNBT.setInteger(BackpackWrapper.MAIN_COLOR, mainColor);
        }

        if (accentColor >= 0) {
            backpackNBT.setInteger(BackpackWrapper.ACCENT_COLOR, accentColor);
        }

        root.setTag(BackpackWrapper.BACKPACK_NBT, backpackNBT);
        result.setTagCompound(root);

        return result;
    }
}
