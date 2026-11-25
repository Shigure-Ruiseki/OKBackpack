package ruiseki.okbackpack.common.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.ShapedOreRecipe;

import ruiseki.okbackpack.common.block.BackpackWrapper;

public class BackpackUpgradeRecipe extends ShapedOreRecipe {

    private final int backpackSlots;
    private final int upgradeSlots;

    public BackpackUpgradeRecipe(ItemStack result, int backpackSlots, int upgradeSlots, Object... recipe) {
        super(result, recipe);
        this.backpackSlots = backpackSlots;
        this.upgradeSlots = upgradeSlots;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {

        ItemStack result = super.getCraftingResult(inv);
        if (result == null) return null;

        NBTTagCompound backpackNBT = new NBTTagCompound();

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

        backpackNBT.setInteger(BackpackWrapper.BACKPACK_SLOTS, backpackSlots);
        backpackNBT.setInteger(BackpackWrapper.UPGRADE_SLOTS, upgradeSlots);

        NBTTagCompound root = new NBTTagCompound();
        root.setTag(BackpackWrapper.BACKPACK_NBT, backpackNBT);

        result.setTagCompound(root);

        return result;
    }
}
