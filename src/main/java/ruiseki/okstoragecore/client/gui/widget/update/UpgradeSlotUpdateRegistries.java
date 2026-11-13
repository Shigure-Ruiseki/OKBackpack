package ruiseki.okstoragecore.client.gui.widget.update;

import ruiseki.okstoragecore.client.gui.widget.filter.AdvancedFilterLogicUpdate;
import ruiseki.okstoragecore.client.gui.widget.filter.AdvancedFluidFilterLogicUpdate;
import ruiseki.okstoragecore.client.gui.widget.filter.BasicFilterLogicUpdate;
import ruiseki.okstoragecore.client.gui.widget.filter.BasicFluidFilterLogicUpdate;
import ruiseki.okstoragecore.upgrades.battery.BatterySlotUpdate;
import ruiseki.okstoragecore.upgrades.crafting.CraftingSlotUpdate;
import ruiseki.okstoragecore.upgrades.tank.TankSlotUpdate;

public class UpgradeSlotUpdateRegistries {

    public static void load() {
        UpgradeSlotUpdateRegistry.register(new BasicFilterLogicUpdate());
        UpgradeSlotUpdateRegistry.register(new AdvancedFilterLogicUpdate());
        UpgradeSlotUpdateRegistry.register(new CraftingSlotUpdate());
        UpgradeSlotUpdateRegistry.register(new BatterySlotUpdate());
        UpgradeSlotUpdateRegistry.register(new TankSlotUpdate());
        UpgradeSlotUpdateRegistry.register(new BasicFluidFilterLogicUpdate());
        UpgradeSlotUpdateRegistry.register(new AdvancedFluidFilterLogicUpdate());
    }
}
