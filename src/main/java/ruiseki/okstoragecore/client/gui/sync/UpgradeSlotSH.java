package ruiseki.okstoragecore.client.gui.sync;

import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;
import ruiseki.okstoragecore.upgrades.IUpgradeWrapper;

public class UpgradeSlotSH extends StorageSlotSH {

    public UpgradeSlotSH(ModularSlot slot, StoragePanel panel) {
        super(slot, panel);
    }

    public IUpgradeWrapper getUpgradeWrapper() {
        return this.getWrapper()
            .getUpgradeHandler()
            .getSlotWrapper(
                this.getSlot()
                    .getSlotIndex());
    }

    public UpgradeSlotUpdate getUpgradeUpdate() {
        return this.getPanel()
            .getUpgradeSlotUpdates()[this.getSlotIndex()];
    }
}
