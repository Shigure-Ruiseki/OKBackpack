package ruiseki.okstoragecore.upgrades;

import net.minecraft.item.ItemStack;

import ruiseki.okcore.item.handler.IItemHandlerSimpleInserter;

public interface IInsertResponseUpgrade {

    ItemStack onBeforeInsert(IItemHandlerSimpleInserter inventoryHandler, int slot, ItemStack stack, boolean simulate);

    void onAfterInsert(IItemHandlerSimpleInserter inventoryHandler, int slot);
}
