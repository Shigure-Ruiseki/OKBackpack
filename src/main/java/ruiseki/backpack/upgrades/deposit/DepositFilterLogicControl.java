package ruiseki.backpack.upgrades.deposit;

import java.util.Arrays;
import java.util.List;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.backpack.client.gui.sync.OBUpgradeSlotSHRegistries;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.widget.filter.ContentsFilterControl;
import ruiseki.okstoragecore.client.gui.widget.filter.FilterLogicControl;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.PrimaryMatch;

public class DepositFilterLogicControl extends FilterLogicControl {

    private static final List<CyclicVariantButtonWidget.Variant> DEPOSIT_FILTER_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.okstoragecore.upgrades.buttons.allow"), OSCTextures.CHECK),
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.okstoragecore.upgrades.buttons.block"), OSCTextures.CROSS),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okbackpack.upgrades.buttons.deposit_filter_type_inventory"),
            OSCTextures.STORAGE));

    private CyclicVariantButtonWidget depositButton;

    public DepositFilterLogicControl(int slotIndex, IFilteredUpgrade filteredWrapper, String syncKey, int slotCount,
        MatchButton... showMatchButtons) {
        super(slotIndex, filteredWrapper, syncKey, slotCount, showMatchButtons);
    }

    @Override
    protected Flow addButtonRow() {
        Flow row = super.addButtonRow();
        row.addChild(addContentsFilterButton(), 0);
        return row;
    }

    protected ButtonWidget<?> addContentsFilterButton() {
        this.depositButton = new CyclicVariantButtonWidget(
            DEPOSIT_FILTER_VARIANTS,
            getContentsFilterLogic().getDepositFilterType()
                .ordinal(),
            this::setDepositFilterType);
        return this.depositButton;
    }

    @Override
    protected void onOreDictsMatchSelected() {
        if (this.getContentsFilterLogic()
            .getDepositFilterType() == DepositFilterType.INVENTORY) {
            this.setDepositFilterType(DepositFilterType.ALLOW);
        }
    }

    protected void setDepositFilterType(int index) {
        DepositFilterType next = DepositFilterType.fromOrdinal(index);
        if (getFilterLogic().getPrimaryMatch() == PrimaryMatch.ORE_DICT && next == DepositFilterType.INVENTORY) {
            next = DepositFilterType.ALLOW;
        }
        this.setDepositFilterType(next);
    }

    private void setDepositFilterType(DepositFilterType filterType) {
        this.getContentsFilterLogic()
            .setDepositFilterType(filterType);
        this.depositButton.setIndex(filterType.ordinal());
        this.getSyncHandler()
            .syncToServer(OBUpgradeSlotSHRegistries.DEPOSIT_TYPE, buffer -> buffer.writeInt(filterType.ordinal()));
    }

    protected DepositFilterLogic getContentsFilterLogic() {
        return (DepositFilterLogic) this.getFilterLogic();
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
