package ruiseki.okbackpack.client.gui.widget.updateGroup;

import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;

import ruiseki.okbackpack.api.widget.IUpgradeSlotGroupFactory;
import ruiseki.okbackpack.common.item.pump.AdvancedPumpUpgradeWrapper;
import ruiseki.okbackpack.common.item.pump.FluidFilterTank;

public class AdvancedPumpFilterSlotGroupFactory implements IUpgradeSlotGroupFactory {

    public static final int FILTER_SIZE = AdvancedPumpUpgradeWrapper.FILTER_SLOTS;
    public static final String SYNC_KEY_PREFIX = "adv_pump_fluid_filter";
    public static final String GROUP_HANDLERS_KEY = "adv_pump_fluid_filter_handlers";
    public static final String GROUP_TANKS_KEY = "adv_pump_fluid_filter_tanks";

    @Override
    public void build(UpgradeSlotUpdateGroup group) {
        FluidSlotSyncHandler[] handlers = new FluidSlotSyncHandler[FILTER_SIZE];
        FluidFilterTank[] tanks = new FluidFilterTank[FILTER_SIZE];
        for (int i = 0; i < FILTER_SIZE; i++) {
            FluidFilterTank tank = new FluidFilterTank(group.wrapper, group.slotIndex, i);
            FluidSlotSyncHandler handler = new FluidSlotSyncHandler(tank).phantom(true)
                .controlsAmount(false);
            group.syncManager.syncValue(SYNC_KEY_PREFIX + "_" + group.slotIndex, i, handler);
            handlers[i] = handler;
            tanks[i] = tank;
        }
        group.put(GROUP_HANDLERS_KEY, handlers);
        group.put(GROUP_TANKS_KEY, tanks);
    }
}
