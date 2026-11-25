package ruiseki.okbackpack.api;

import net.minecraft.inventory.Container;

import ruiseki.okbackpack.client.gui.handler.IndexedInventoryCraftingWrapper;
import ruiseki.okbackpack.client.gui.slot.IndexedModularCraftingSlot;

public interface IStorageContainer<T extends Container> {

    T getContainer();

    void registerCraftingSlot(int slotIndex, IndexedModularCraftingSlot craftingSlot);

    void registerInventoryCrafting(int slotIndex, IndexedInventoryCraftingWrapper inventoryCrafting);

}
