package ruiseki.backpack.client.gui.sync;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.backpack.backpack.BackpackPanel;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.okstoragecore.client.gui.sync.StorageSlotSH;

public class BackpackSlotSH extends StorageSlotSH {

    public BackpackSlotSH(ModularSlot slot, BackpackPanel panel) {
        super(slot, panel);
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
