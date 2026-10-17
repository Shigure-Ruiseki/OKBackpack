package ruiseki.backpack.client.gui.container;

import ruiseki.backpack.backpack.BackpackPanel;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.storagecore.client.gui.container.StorageContainer;

public class BackpackContainer extends StorageContainer {

    public BackpackContainer(IBackpackWrapper wrapper, BackpackPanel panel) {
        super(wrapper, panel);
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
