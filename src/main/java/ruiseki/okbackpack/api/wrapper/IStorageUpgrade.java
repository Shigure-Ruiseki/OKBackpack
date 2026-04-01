package ruiseki.okbackpack.api.wrapper;

import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;

public interface IStorageUpgrade {

    String STORAGE_TAG = "Storage";

    UpgradeItemStackHandler getStorage();
}
