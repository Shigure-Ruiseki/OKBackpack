package ruiseki.okstoragecore.client.gui.widget.filter;

import ruiseki.okstoragecore.upgrades.IFluidFilteredUpgrade;

public class FluidFilterLogicControl extends FluidFilterLogicControlBase {

    public FluidFilterLogicControl(int slotIndex, IFluidFilteredUpgrade filteredWrapper, String syncKey, int slotCount,
        MatchButton... showMatchButtons) {
        super(slotIndex, filteredWrapper, syncKey, slotCount, showMatchButtons);
    }

    public static class Basic extends FluidFilterLogicControl {

        public Basic(int slotIndex, IFluidFilteredUpgrade filteredWrapper, String syncKey) {
            super(slotIndex, filteredWrapper, syncKey, 9, MatchButton.ALLOW_LIST);
        }
    }

    public static class Advanced extends FluidFilterLogicControl {

        public Advanced(int slotIndex, IFluidFilteredUpgrade filteredWrapper, String syncKey) {
            super(slotIndex, filteredWrapper, syncKey, 16, MatchButton.ALLOW_LIST);
        }
    }
}
