package ruiseki.okstoragecore.client.gui.widget.filter;

import java.util.Arrays;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.upgrades.ContentsFilterLogic;
import ruiseki.okstoragecore.upgrades.ContentsFilterType;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.PrimaryMatch;

public class ContentsFilterControl extends FilterLogicControl {

    private static final List<CyclicVariantButtonWidget.Variant> CONTENTS_FILTER_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.okstoragecore.upgrades.buttons.allow"), OSCTextures.CHECK),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.okstoragecore.upgrades.buttons.block"), OSCTextures.CROSS),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.inventory"),
            OSCTextures.BACKPACK));

    private CyclicVariantButtonWidget contentsButton;

    public ContentsFilterControl(int slotIndex, IFilteredUpgrade filteredWrapper, String syncKey, int slotCount,
        MatchButton... showMatchButtons) {
        super(slotIndex, filteredWrapper, syncKey, slotCount, showMatchButtons);
        this.setSlotsDisabled(() -> getContentsFilterLogic().getFilterType() == ContentsFilterType.STORAGE);
    }

    @Override
    protected Flow addButtonRow() {
        Flow row = super.addButtonRow();
        row.addChild(addContentsFilterButton(), 0);
        return row;
    }

    protected ButtonWidget<?> addContentsFilterButton() {
        this.contentsButton = new CyclicVariantButtonWidget(
            CONTENTS_FILTER_VARIANTS,
            getContentsFilterLogic().getFilterType()
                .ordinal(),
            this::setContentsFilterType);
        return this.contentsButton;
    }

    @Override
    protected void onOreDictsMatchSelected() {
        if (this.getContentsFilterLogic()
            .getFilterType() == ContentsFilterType.STORAGE) {
            this.setContentsFilterType(ContentsFilterType.ALLOW);
        }
    }

    protected void setContentsFilterType(int index) {
        ContentsFilterType next = ContentsFilterType.fromOrdinal(index);
        if (getFilterLogic().getPrimaryMatch() == PrimaryMatch.ORE_DICT && next == ContentsFilterType.STORAGE) {
            next = ContentsFilterType.ALLOW;
        }
        this.setContentsFilterType(next);
    }

    private void setContentsFilterType(ContentsFilterType filterType) {
        this.getContentsFilterLogic()
            .setDepositFilterType(filterType);
        this.contentsButton.setIndex(filterType.ordinal());
        this.getSyncHandler()
            .syncToServer(UpgradeSlotSHRegistries.CONTENTS_FILTER, buffer -> buffer.writeInt(filterType.ordinal()));
    }

    protected ContentsFilterLogic getContentsFilterLogic() {
        return (ContentsFilterLogic) this.getFilterLogic();
    }

    public static class Basic extends ContentsFilterControl {

        public Basic(int slotIndex, IFilteredUpgrade filteredWrapper, String syncKey) {
            super(slotIndex, filteredWrapper, syncKey, 9);
        }
    }

    public static class Advanced extends ContentsFilterControl {

        public Advanced(int slotIndex, IFilteredUpgrade filteredWrapper, String syncKey) {
            super(
                slotIndex,
                filteredWrapper,
                syncKey,
                16,
                MatchButton.PRIMARY_MATCH,
                MatchButton.DURABILITY,
                MatchButton.NBT);
        }
    }
}
