package ruiseki.okstoragecore.client.gui.widget.update;

import ruiseki.okstoragecore.client.gui.widget.filter.AdvancedFilterLogicUpdate;
import ruiseki.okstoragecore.client.gui.widget.filter.BasicFilterLogicUpdate;
import ruiseki.okstoragecore.upgrades.battery.BatterySlotUpdate;
import ruiseki.okstoragecore.upgrades.crafting.CraftingSlotUpdate;

public class UpgradeSlotUpdateRegistries {

    public static void load() {
        UpgradeSlotUpdateRegistry.register(new BasicFilterLogicUpdate());
        UpgradeSlotUpdateRegistry.register(new AdvancedFilterLogicUpdate());
        UpgradeSlotUpdateRegistry.register(new CraftingSlotUpdate());
        UpgradeSlotUpdateRegistry.register(new BatterySlotUpdate());
    }
}
