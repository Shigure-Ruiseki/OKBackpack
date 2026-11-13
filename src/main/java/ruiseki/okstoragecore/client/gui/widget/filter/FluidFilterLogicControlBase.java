package ruiseki.okstoragecore.client.gui.widget.filter;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.ISyncOrValue;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;

import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.widget.StorageSlotGrid;
import ruiseki.okstoragecore.upgrades.FluidFilterLogic;
import ruiseki.okstoragecore.upgrades.IFluidFilteredUpgrade;

public class FluidFilterLogicControlBase extends ParentWidget<FluidFilterLogicControlBase> {

    private static final List<CyclicVariantButtonWidget.Variant> ALLOW_LIST_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(IKey.lang("gui.okstoragecore.upgrades.buttons.allow"), OSCTextures.CHECK),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.block"),
            OSCTextures.CROSS));

    protected final IFluidFilteredUpgrade filteredWrapper;
    protected final MatchButton[] showMatchButtons;
    protected final int slotIndex;
    protected final String syncKey;
    protected final int slotCount;

    protected BooleanSupplier slotsDisabled = () -> false;

    public FluidFilterLogicControlBase(int slotIndex, IFluidFilteredUpgrade filteredWrapper, String syncKey,
        int slotCount, MatchButton... showMatchButtons) {
        this.slotIndex = slotIndex;
        this.showMatchButtons = showMatchButtons;
        this.syncKey = syncKey;
        this.slotCount = slotCount;
        this.filteredWrapper = filteredWrapper;
        this.coverChildren();

        this.syncHandler("upgrades", this.getSlotIndex());
        this.child(this.addContentColumn());
    }

    protected Flow addContentColumn() {
        return Flow.column()
            .coverChildren()
            .childPadding(4)
            .collapseDisabledChild()
            .crossAxisAlignment(Alignment.CrossAxis.START)
            .child(this.addButtonRow())
            .child(this.addFluidFilterGroup());
    }

    protected Flow addButtonRow() {
        Flow buttonRow = Flow.row()
            .coverChildren()
            .crossAxisAlignment(Alignment.CrossAxis.START)
            .childPadding(2);
        if (this.shouldShow(MatchButton.ALLOW_LIST)) {
            buttonRow.child(this.addAllowListButton());
        }
        return buttonRow;
    }

    protected ButtonWidget<?> addAllowListButton() {
        return new CyclicVariantButtonWidget(
            ALLOW_LIST_VARIANTS,
            this.filteredWrapper.getFilterLogic()
                .isAllowList() ? 0 : 1,
            index -> {
                this.filteredWrapper.getFilterLogic()
                    .setAllowList(index == 0);
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.ALLOW_LIST_FLUID, buffer -> buffer.writeBoolean(index == 0));
            });
    }

    protected Flow addFluidFilterGroup() {
        final int columns = 4;
        final int rows = (this.slotCount + columns - 1) / columns;
        StorageSlotGrid<FluidSlot> slotGrid = StorageSlotGrid.<FluidSlot>builder()
            .matrix(columns, rows, 'F')
            .maxSlots(this.slotCount)
            .key(
                'F',
                i -> new FluidSlot().name(this.getSyncKey() + "_" + this.getSlotIndex() + "_" + i)
                    .syncHandler(this.getSyncKey() + "_" + this.getSlotIndex(), i))
            .build();
        return Flow.column()
            .coverChildren()
            .child(slotGrid);
    }

    protected boolean shouldShow(MatchButton matchButton) {
        for (MatchButton showMatchButton : showMatchButtons) {
            if (showMatchButton == matchButton) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue instanceof UpgradeSlotSH;
    }

    @Override
    public @NotNull UpgradeSlotSH getSyncHandler() {
        return (UpgradeSlotSH) super.getSyncHandler();
    }

    public enum MatchButton {
        ALLOW_LIST
    }

    public IFluidFilteredUpgrade getFilteredWrapper() {
        return filteredWrapper;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public int getSlotCount() {
        return slotCount;
    }

    public BooleanSupplier getSlotsDisabled() {
        return slotsDisabled;
    }

    public MatchButton[] getShowMatchButtons() {
        return showMatchButtons;
    }

    public String getSyncKey() {
        return syncKey;
    }

    public FluidFilterLogic getFilterLogic() {
        return this.getFilteredWrapper()
            .getFilterLogic();
    }
}
