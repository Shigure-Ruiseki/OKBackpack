package ruiseki.okstoragecore.client.gui.slot;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import ruiseki.okcore.client.mui.gui.component.slot.ModularItemSlot;
import ruiseki.okcore.item.handler.IItemHandler;
import ruiseki.okstoragecore.api.ISlotChangeResponseUpgrade;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class ModularStorageSlot extends ModularItemSlot {

    protected final IStorageWrapper wrapper;
    protected final StoragePanel panel;

    public ModularStorageSlot(int index, IStorageWrapper wrapper, StoragePanel panel) {
        super(wrapper.getInventoryHandler(), index);
        this.wrapper = wrapper;
        this.panel = panel;
    }

    @Override
    public int getSlotStackLimit() {
        return this.wrapper.getInventoryHandler()
            .getBaseSlotLimit();
    }

    @Override
    public void onSlotChanged() {
        super.onSlotChanged();
        // saving here as well because there are many cases where vanilla modifies stack directly without and inventory
        // handler isn't aware of it
        // however it does notify the slot of change
        this.wrapper.getInventoryHandler()
            .onContentsChanged(this.getSlotIndex());
        this.processSlotChangeResponse(this.getSlotIndex(), wrapper.getInventoryHandler(), wrapper);
    }

    protected void processSlotChangeResponse(int slot, IItemHandler handler, IStorageWrapper storageWrapper) {
        if (!this.panel.getPlayer().worldObj.isRemote) {
            storageWrapper.getUpgradeHandler()
                .getWrappersThatImplementFromMainStorage(ISlotChangeResponseUpgrade.class)
                .forEach(u -> u.onSlotChange(handler, slot));
        }
    }

    @Override
    public int getItemStackLimit(@NotNull ItemStack stack) {
        return this.wrapper.getInventoryHandler()
            .getStackLimit(this.getSlotIndex(), stack);
    }
}
