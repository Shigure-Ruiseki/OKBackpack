package ruiseki.storagecore.client.gui.slot;

import ruiseki.okcore.item.handler.IItemHandler;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.StoragePanel;

public class ModularUpgradeSlot extends ModularStorageSlot {

    public ModularUpgradeSlot(IItemHandler itemHandler, int index, IStorageWrapper wrapper, StoragePanel panel) {
        super(itemHandler, index, wrapper, panel);
    }
}
