package ruiseki.backpack.client.gui.sync;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.backpack.backpack.BackpackPanel;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.storagecore.client.gui.sync.StorageSlotSH;

public class BackpackSlotSH extends StorageSlotSH {

    public BackpackSlotSH(ModularSlot slot, IBackpackWrapper wrapper, BackpackPanel panel) {
        super(slot, wrapper, panel);
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
