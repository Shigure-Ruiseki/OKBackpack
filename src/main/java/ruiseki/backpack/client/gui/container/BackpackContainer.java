package ruiseki.backpack.client.gui.container;

import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.storagecore.client.gui.container.StorageContainer;

public class BackpackContainer extends StorageContainer {

    protected final IBackpackWrapper wrapper;

    public BackpackContainer(IBackpackWrapper wrapper) {
        super(wrapper);
        this.wrapper = wrapper;
    }
}
