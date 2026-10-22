package ruiseki.okstoragecore.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class UpgradeContainer extends Container {

    private final IStorageWrapper storageWrapper;
    private final StoragePanel storagePanel;
    private final EntityPlayer player;
    private final int slotIndex;

    public UpgradeContainer(IStorageWrapper storageWrapper, StoragePanel storagePanel, EntityPlayer player,
        int upgradeIndex) {
        this.storageWrapper = storageWrapper;
        this.storagePanel = storagePanel;
        this.player = player;
        this.slotIndex = upgradeIndex;
    }

    public IStorageWrapper getStorageWrapper() {
        return storageWrapper;
    }

    public StoragePanel getStoragePanel() {
        return storagePanel;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }

    public void onAdded(UpgradeSlotUpdate update) {

    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {

    }

    @Override
    public void detectAndSendChanges() {

    }

    @Override
    public void onContainerClosed(EntityPlayer player) {

    }
}
