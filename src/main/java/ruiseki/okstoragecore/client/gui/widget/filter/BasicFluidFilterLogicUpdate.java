package ruiseki.okstoragecore.client.gui.widget.filter;

import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;

import ruiseki.okstoragecore.client.gui.sync.DelegatedFluidFilterLogicSH;
import ruiseki.okstoragecore.client.gui.sync.value.DelegatedFluidHandler;
import ruiseki.okstoragecore.client.gui.widget.update.IUpgradeSlotUpdateFactory;
import ruiseki.okstoragecore.client.gui.widget.update.UpgradeSlotUpdate;

public class BasicFluidFilterLogicUpdate implements IUpgradeSlotUpdateFactory {

    @Override
    public void build(UpgradeSlotUpdate group) {

        DelegatedFluidFilterLogicSH logicSH = new DelegatedFluidFilterLogicSH(
            group.getPanel(),
            group::getContainer,
            group.getUpgradeIndex(),
            9);
        group.getSyncManager()
            .syncValue("basic_fluid_filter_delegation_" + group.getUpgradeIndex(), logicSH);
        group.put("basic_fluid_filter_logic", logicSH);

        FluidSlotSyncHandler[] slotSHs = new FluidSlotSyncHandler[9];
        DelegatedFluidHandler handler = logicSH.getFluidHandler();
        for (int i = 0; i < 9; i++) {
            FluidSlotSyncHandler slotSH = new FluidSlotSyncHandler(handler.getTank(i)).phantom(true)
                .controlsAmount(false);
            group.getSyncManager()
                .syncValue("basic_fluid_filter_" + group.getUpgradeIndex(), i, slotSH);
            slotSHs[i] = slotSH;
        }
        group.put("basic_fluid_filter_slot_shs", slotSHs);
    }
}
