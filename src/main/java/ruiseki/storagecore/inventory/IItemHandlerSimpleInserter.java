package ruiseki.storagecore.inventory;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.item.handler.IItemHandlerModifiable;

public interface IItemHandlerSimpleInserter extends IItemHandlerModifiable {

    ItemStack insertItem(ItemStack stack, boolean simulate);
}
