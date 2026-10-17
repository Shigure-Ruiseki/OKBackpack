package ruiseki.storagecore.client.gui.sync;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.storagecore.api.IStorageWrapper;
import ruiseki.storagecore.client.gui.widget.StoragePanel;
import ruiseki.storagecore.upgrades.IUpgradeWrapper;

public class UpgradeSlotSH extends StorageSlotSH {

    public UpgradeSlotSH(ModularSlot slot, IStorageWrapper wrapper, StoragePanel panel) {
        super(slot, wrapper, panel);
    }

    public IUpgradeWrapper getUpgradeWrapper() {
        return this.getWrapper()
            .getUpgradeHandler()
            .getSlotWrapper(
                this.getSlot()
                    .getSlotIndex());
    }
}
