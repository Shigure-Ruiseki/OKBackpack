package ruiseki.backpack.client.gui.container;

import ruiseki.backpack.backpack.BackpackPanel;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
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
