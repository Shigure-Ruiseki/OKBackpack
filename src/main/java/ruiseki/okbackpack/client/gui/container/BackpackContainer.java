package ruiseki.okbackpack.client.gui.container;

import ruiseki.okbackpack.backpack.BackpackPanel;
import ruiseki.okbackpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.okstoragecore.client.gui.container.StorageContainer;

public class BackpackContainer extends StorageContainer {

    public BackpackContainer(BackpackPanel panel) {
        super(panel);
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
