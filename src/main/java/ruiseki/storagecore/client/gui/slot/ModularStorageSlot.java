package ruiseki.storagecore.client.gui.slot;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okcore.client.mui.gui.component.slot.ModularItemSlot;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.StoragePanel;
import ruiseki.storagecore.inventory.InventoryHandler;

public class ModularStorageSlot extends ModularItemSlot {

    protected final InventoryHandler inventoryHandler;
    protected final IStorageWrapper wrapper;
    protected final StoragePanel panel;

    public ModularStorageSlot(InventoryHandler inventoryHandler, int index, IStorageWrapper wrapper,
        StoragePanel panel) {
        super(inventoryHandler, index);
        this.inventoryHandler = inventoryHandler;
        this.wrapper = wrapper;
        this.panel = panel;
    }

    @Override
    public int getSlotStackLimit() {
        return this.inventoryHandler.getBaseSlotLimit();
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        return this.inventoryHandler.getStackLimit(getSlotIndex(), stack);
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
    }
}
