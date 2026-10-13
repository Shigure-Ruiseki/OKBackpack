package ruiseki.storagecore.client.gui.container;

import com.cleanroommc.modularui.screen.ModularContainer;

import ruiseki.storagecore.api.IStorageWrapper;

public class StorageContainer extends ModularContainer {

    protected final IStorageWrapper wrapper;

    public StorageContainer(IStorageWrapper wrapper) {
        this.wrapper = wrapper;
    }
}
