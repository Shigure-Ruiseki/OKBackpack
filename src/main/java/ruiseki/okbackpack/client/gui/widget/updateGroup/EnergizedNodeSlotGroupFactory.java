package ruiseki.okbackpack.client.gui.widget.updateGroup;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;

public class EnergizedNodeSlotGroupFactory implements IUpgradeSlotGroupFactory {

    @Override
    public void build(UpgradeSlotUpdateGroup group) {
        // The energized node upgrade no longer exposes any widget slots or synced parameters.
    }
}
