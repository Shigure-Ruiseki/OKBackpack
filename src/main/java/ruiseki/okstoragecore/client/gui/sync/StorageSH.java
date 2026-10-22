package ruiseki.okstoragecore.client.gui.sync;

import net.minecraft.entity.player.EntityPlayer;

import ruiseki.okcore.item.capability.wrapper.PlayerMainInvWrapper;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class StorageSH extends SyncHandlerBase<StorageSH> {

    private final PlayerMainInvWrapper playerInventory;
    private final StoragePanel panel;
    private final IStorageWrapper wrapper;

    public StorageSH(PlayerMainInvWrapper playerInventory, IStorageWrapper wrapper, StoragePanel panel) {
        this.playerInventory = playerInventory;
        this.panel = panel;
        this.wrapper = wrapper;
    }

    public IStorageWrapper getWrapper() {
        return wrapper;
    }

    public StoragePanel getPanel() {
        return panel;
    }

    public PlayerMainInvWrapper getPlayerInventory() {
        return playerInventory;
    }

    public EntityPlayer getPlayer() {
        return this.getPanel()
            .getPlayer();
    }
}
