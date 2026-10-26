package ruiseki.backpack.client.gui.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import ruiseki.backpack.backpack.BackpackAccessLogger;
import ruiseki.backpack.backpack.BackpackPanel;
import ruiseki.backpack.backpack.UUIDDeduplicator;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.okstoragecore.client.gui.container.StorageContainer;

public class BackpackContainer extends StorageContainer {

    public BackpackContainer(EntityPlayer player, IBackpackWrapper wrapper, BackpackPanel panel) {
        super(player, wrapper, panel);

        this.getStorageWrapper()
            .getContentsUuid()
            .ifPresent(backpackUuid -> {
                ItemStack backpack = this.getStorageWrapper()
                    .getBackpack();
                BackpackAccessLogger.logPlayerAccess(
                    player,
                    backpack.getItem(),
                    backpackUuid,
                    backpack.getDisplayName(),
                    storageWrapper.getMainColor(),
                    storageWrapper.getAccentColor(),
                    storageWrapper.getColumnsTaken());

                if (!player.worldObj.isRemote) {
                    UUIDDeduplicator.checkForDuplicateBackpacksAndRemoveTheirUUID(
                        player,
                        backpackUuid,
                        this.getStorageWrapper()
                            .getBackpack());
                }
            });
    }

    @Override
    public IBackpackWrapper getStorageWrapper() {
        return (IBackpackWrapper) super.getStorageWrapper();
    }

    @Override
    public BackpackPanel getStoragePanel() {
        return (BackpackPanel) super.getStoragePanel();
    }
}
