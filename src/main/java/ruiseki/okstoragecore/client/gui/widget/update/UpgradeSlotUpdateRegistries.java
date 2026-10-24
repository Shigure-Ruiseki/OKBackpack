package ruiseki.okstoragecore.client.gui.widget.update;

import ruiseki.okstoragecore.client.gui.widget.filter.AdvancedFilterLogicUpdate;
import ruiseki.okstoragecore.client.gui.widget.filter.BasicFilterLogicUpdate;

public class UpgradeSlotUpdateRegistries {

    public static void load() {
        UpgradeSlotUpdateRegistry.register(new BasicFilterLogicUpdate());
        UpgradeSlotUpdateRegistry.register(new AdvancedFilterLogicUpdate());
    }
}
