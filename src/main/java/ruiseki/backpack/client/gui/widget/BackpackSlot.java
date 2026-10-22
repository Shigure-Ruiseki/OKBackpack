package ruiseki.backpack.client.gui.widget;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.StorageSlot;

public class BackpackSlot extends StorageSlot {

    public BackpackSlot(IStorageWrapper storageWrapper, StoragePanel storagePanel, int slotIndex) {
        super(storageWrapper, storagePanel, slotIndex);
    }
}
