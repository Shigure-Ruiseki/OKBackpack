package ruiseki.storagecore.client.gui.slot;

import ruiseki.okcore.client.mui.gui.component.slot.ModularItemSlot;
import ruiseki.okcore.item.handler.IItemHandler;
import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.StoragePanel;

public class ModularStorageSlot extends ModularItemSlot {

    protected final IStorageWrapper wrapper;
    protected final StoragePanel panel;

    public ModularStorageSlot(IItemHandler itemHandler, int index, IStorageWrapper wrapper, StoragePanel panel) {
        super(itemHandler, index);
        this.wrapper = wrapper;
        this.panel = panel;
    }
}
