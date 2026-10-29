package ruiseki.backpack.client.gui.sync;

import ruiseki.backpack.backpack.BackpackPanel;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.okcore.item.capability.wrapper.PlayerMainInvWrapper;
import ruiseki.okstoragecore.client.gui.sync.StorageSH;

public class BackpackSH extends StorageSH {

    public BackpackSH(PlayerMainInvWrapper playerInventory, BackpackPanel panel) {
        super(playerInventory, panel);
    }

    @Override
    public IBackpackWrapper getWrapper() {
        return this.getPanel()
            .getWrapper();
    }

    @Override
    public BackpackPanel getPanel() {
        return (BackpackPanel) super.getPanel();
    }
}
