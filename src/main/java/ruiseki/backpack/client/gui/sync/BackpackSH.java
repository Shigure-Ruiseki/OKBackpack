package ruiseki.backpack.client.gui.sync;

import ruiseki.backpack.backpack.BackpackPanel;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.okcore.item.capability.wrapper.PlayerMainInvWrapper;
import ruiseki.storagecore.client.gui.sync.StorageSH;

public class BackpackSH extends StorageSH {

    public BackpackSH(PlayerMainInvWrapper playerInventory, IBackpackWrapper wrapper, BackpackPanel panel) {
        super(playerInventory, wrapper, panel);
    }

    @Override
    public IBackpackWrapper getWrapper() {
        return (IBackpackWrapper) super.getWrapper();
    }

    @Override
    public BackpackPanel getPanel() {
        return (BackpackPanel) super.getPanel();
    }
}
