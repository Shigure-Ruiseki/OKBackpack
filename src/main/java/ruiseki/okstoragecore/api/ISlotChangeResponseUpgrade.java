package ruiseki.okstoragecore.api;

import ruiseki.okcore.item.handler.IItemHandler;

public interface ISlotChangeResponseUpgrade {

    void onSlotChange(IItemHandler inventoryHandler, int slot);
}
