package ruiseki.okstoragecore.client.gui.widget.filter;

import ruiseki.okstoragecore.upgrades.FilterLogic;

public class FilterLogicControl extends FilterLogicControlBase {

    public FilterLogicControl(int slotIndex, FilterLogic filteredWrapper, String syncKey, int slotCount,
        MatchButton... showMatchButtons) {
        super(slotIndex, filteredWrapper, syncKey, slotCount, showMatchButtons);
    }

    public static class Basic extends FilterLogicControl {

        public Basic(int slotIndex, FilterLogic filteredWrapper, String syncKey) {
            super(slotIndex, filteredWrapper, syncKey, 9, MatchButton.ALLOW_LIST);
        }
    }

    public static class Advanced extends FilterLogicControl {

        public Advanced(int slotIndex, FilterLogic filteredWrapper, String syncKey) {
            super(
                slotIndex,
                filteredWrapper,
                syncKey,
                16,
                MatchButton.ALLOW_LIST,
                MatchButton.PRIMARY_MATCH,
                MatchButton.DURABILITY,
                MatchButton.NBT);
        }
    }
}
