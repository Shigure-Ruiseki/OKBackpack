package ruiseki.storagecore.upgrades;

import ruiseki.okcore.item.handler.IItemHandlerModifiable;

public interface IStorageUpgrade {

    IItemHandlerModifiable getStorage();
}
