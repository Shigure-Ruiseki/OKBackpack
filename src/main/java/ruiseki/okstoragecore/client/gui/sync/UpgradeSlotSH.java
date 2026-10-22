package ruiseki.okstoragecore.client.gui.sync;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

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
