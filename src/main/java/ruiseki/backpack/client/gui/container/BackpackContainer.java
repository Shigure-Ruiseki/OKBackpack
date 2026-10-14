package ruiseki.backpack.client.gui.container;

import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.storagecore.client.gui.container.StorageContainer;

public class BackpackContainer extends StorageContainer {

    public BackpackContainer(IBackpackWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public IBackpackWrapper getWrapper() {
        return (IBackpackWrapper) super.getWrapper();
    }
}
