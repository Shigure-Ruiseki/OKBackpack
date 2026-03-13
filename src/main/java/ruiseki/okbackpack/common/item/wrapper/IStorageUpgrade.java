package ruiseki.okbackpack.common.item.wrapper;

import ruiseki.okbackpack.client.gui.handler.UpgradeItemStackHandler;

public interface IStorageUpgrade {

    String STORAGE_TAG = "Storage";

    UpgradeItemStackHandler getStorage();

    void setStorage(UpgradeItemStackHandler handler);
}
