package ruiseki.okstoragecore.client.gui.widget.filter;

import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;

import ruiseki.okstoragecore.client.gui.sync.DelegatedFluidFilterLogicSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedFluidHandler;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class BasicFluidFilterLogicUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate slotUpdate) {

        DelegatedFluidFilterLogicSH logicSH = new DelegatedFluidFilterLogicSH(
            slotUpdate.getPanel(),
            slotUpdate::getContainer,
            slotUpdate.getUpgradeIndex(),
            9);
        slotUpdate.getSyncManager()
            .syncValue("basic_fluid_filter_delegation_" + slotUpdate.getUpgradeIndex(), logicSH);
        slotUpdate.put("basic_fluid_filter_logic", logicSH);

        FluidSlotSyncHandler[] slotSHs = new FluidSlotSyncHandler[9];
        DelegatedFluidHandler handler = logicSH.getFluidHandler();
        for (int i = 0; i < 9; i++) {
            FluidSlotSyncHandler slotSH = new FluidSlotSyncHandler(handler.getTank(i)).phantom(true)
                .controlsAmount(false);
            slotUpdate.getSyncManager()
                .syncValue("basic_fluid_filter_" + slotUpdate.getUpgradeIndex(), i, slotSH);
            slotSHs[i] = slotSH;
        }
        slotUpdate.put("basic_fluid_filter_slot_shs", slotSHs);
    }
}
