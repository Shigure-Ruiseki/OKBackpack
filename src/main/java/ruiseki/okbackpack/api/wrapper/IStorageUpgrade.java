package ruiseki.okbackpack.api.wrapper;

import ruiseki.okbackpack.client.gui.handler.BaseItemStackHandler;

public interface IStorageUpgrade {

    String STORAGE_TAG = "Storage";

    BaseItemStackHandler getStorage();
}
