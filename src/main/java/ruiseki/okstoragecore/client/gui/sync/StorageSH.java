package ruiseki.okstoragecore.client.gui.sync;

import net.minecraft.entity.player.EntityPlayer;

import ruiseki.okcore.item.capability.wrapper.PlayerMainInvWrapper;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class StorageSH extends SyncHandlerBase<StorageSH> {

    private final PlayerMainInvWrapper playerInventory;

    public StorageSH(PlayerMainInvWrapper playerInventory, IStorageWrapper wrapper, StoragePanel panel) {
        super(wrapper, panel);
        this.playerInventory = playerInventory;
    }

    public PlayerMainInvWrapper getPlayerInventory() {
        return playerInventory;
    }

    public EntityPlayer getPlayer() {
        return this.getPanel()
            .getPlayer();
    }
}
