package ruiseki.okstoragecore.client.gui.widget.filter;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.client.gui.slot.DisableablePhantomItemSlot;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSH;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.upgrades.FilterLogic;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.PrimaryMatch;

public class FilterLogicControlBase extends ParentWidget<FilterLogicControlBase> {

    private static final List<CyclicVariantButtonWidget.Variant> ALLOW_LIST_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.allow"),
            OSCTextures.CHECK_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.block"),
            OSCTextures.CROSS_ICON));

    private static final List<CyclicVariantButtonWidget.Variant> MATCH_TYPES_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.match_item"),
            OSCTextures.MATCH_ITEM),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.match_mod"),
            OSCTextures.MATCH_MOD),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.match_ore_dict"),
            OSCTextures.MATCH_TAG),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.match_tag"),
            OSCTextures.MATCH_TAG));

    private static final List<CyclicVariantButtonWidget.Variant> DURABILITY_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.match_durability"),
            OSCTextures.MATCH_DURABILITY),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.ignore_durability"),
            OSCTextures.IGNORE_DURABILITY));

    private static final List<CyclicVariantButtonWidget.Variant> NBT_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.match_nbt"),
            OSCTextures.MATCH_NBT),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.ignore_nbt"),
            OSCTextures.IGNORE_NBT));

    protected final IFilteredUpgrade filteredWrapper;
    protected final MatchButton[] showMatchButtons;
    protected final int slotIndex;
    protected final String syncKey;
    protected final int slotCount;

    protected BooleanSupplier slotsDisabled = () -> false;

    public FilterLogicControlBase(int slotIndex, IFilteredUpgrade filteredWrapper, String syncKey, int slotCount,
        MatchButton... showMatchButtons) {
        this.slotIndex = slotIndex;
        this.showMatchButtons = showMatchButtons;
        this.syncKey = syncKey;
        this.slotCount = slotCount;
        this.filteredWrapper = filteredWrapper;

        // init sync handler
        this.syncHandler("upgrades", slotIndex);

        this.child(this.addButtonRow())
            .child(this.addItemConfigGroup());
    }

    protected Flow addButtonRow() {
        Flow buttonRow = Flow.row()
            .leftRel(0.5f)
            .height(20)
            .coverChildrenWidth()
            .childPadding(2);
        if (this.shouldShow(MatchButton.ALLOW_LIST)) {
            buttonRow.child(this.addAllowListButton());
        }
        if (this.shouldShow(MatchButton.PRIMARY_MATCH)) {
            buttonRow.child(this.addMatchTypesButton());
        }
        buttonRow.child(this.addItemConfigRow());
        return buttonRow;
    }

    protected Flow addItemConfigRow() {
        Flow configRow = Flow.row()
            .childPadding(2)
            .height(20)
            .setEnabledIf(
                row -> this.filteredWrapper.getFilterLogic()
                    .getPrimaryMatch() == PrimaryMatch.ITEM);
        if (this.shouldShow(MatchButton.DURABILITY)) {
            configRow.child(this.addDurabilityButton());
        }
        if (this.shouldShow(MatchButton.NBT)) {
            configRow.child(this.addNBTButton());
        }
        return configRow;
    }

    protected ButtonWidget<?> addAllowListButton() {
        return new CyclicVariantButtonWidget(
            ALLOW_LIST_VARIANTS,
            this.filteredWrapper.getFilterLogic()
                .isAllowList() ? 0 : 1,
            index -> {
                this.filteredWrapper.getFilterLogic()
                    .setAllowList(index == 0);
                this.updateWrapper();
            });
    }

    protected ButtonWidget<?> addMatchTypesButton() {
        return new CyclicVariantButtonWidget(
            MATCH_TYPES_VARIANTS,
            this.filteredWrapper.getFilterLogic()
                .getPrimaryMatch()
                .ordinal(),
            index -> {
                this.filteredWrapper.getFilterLogic()
                    .setPrimaryMatch(PrimaryMatch.fromOrdinal(index));
                this.updateWrapper();
            });
    }

    protected ButtonWidget<?> addDurabilityButton() {
        return new CyclicVariantButtonWidget(
            DURABILITY_VARIANTS,
            this.filteredWrapper.getFilterLogic()
                .shouldMatchDurability() ? 0 : 1,
            index -> {
                this.filteredWrapper.getFilterLogic()
                    .setMatchDurability(index == 0);
                this.updateWrapper();
            }).setInEffect(
                this.filteredWrapper.getFilterLogic()
                    .getPrimaryMatch() == PrimaryMatch.ITEM);
    }

    protected ButtonWidget<?> addNBTButton() {
        return new CyclicVariantButtonWidget(
            NBT_VARIANTS,
            this.filteredWrapper.getFilterLogic()
                .shouldMatchNbt() ? 0 : 1,
            index -> {
                this.filteredWrapper.getFilterLogic()
                    .setMatchNbt(index == 0);
                this.updateWrapper();
            }).setInEffect(
                this.filteredWrapper.getFilterLogic()
                    .getPrimaryMatch() == PrimaryMatch.ITEM);
    }

    protected Flow addItemConfigGroup() {

        // Slots
        SlotGroupWidget slotGroup = new SlotGroupWidget().coverChildren()
            .leftRel(0.5f);

        for (int i = 0; i < this.slotCount; i++) {
            ItemSlot slot = new DisableablePhantomItemSlot(() -> this.slotsDisabled.getAsBoolean());
            slot.name(this.syncKey + "_" + this.slotIndex)
                .syncHandler(this.syncKey + "_" + this.slotIndex, i)
                .pos(i % 4 * 18, i / 4 * 18);
            slotGroup.child(slot);
        }

        int filterSlotRows = (this.slotCount + 3) / 4;
        int itemGroupHeight = filterSlotRows * 18 + 13;

        return Flow.column()
            .size(88, itemGroupHeight)
            .leftRel(0.5f)
            .top(24)
            .child(slotGroup)
            .setEnabledIf(
                flow -> this.filteredWrapper.getFilterLogic()
                    .getPrimaryMatch() != PrimaryMatch.ORE_DICT);
    }

    protected boolean shouldShow(MatchButton matchButton) {
        for (MatchButton showMatchButton : showMatchButtons) {
            if (showMatchButton == matchButton) {
                return true;
            }
        }
        return false;
    }

    public void setSlotsDisabled(BooleanSupplier disabled) {
        this.slotsDisabled = disabled;
    }

    protected void updateWrapper() {
        this.getSyncHandler()
            .syncToServer(UpgradeSlotSHRegistries.FILTER_LOGIC, buf -> {});
    }

    @Override
    public @NotNull UpgradeSlotSH getSyncHandler() {
        return (UpgradeSlotSH) super.getSyncHandler();
    }

    public enum MatchButton {
        ALLOW_LIST,
        PRIMARY_MATCH,
        DURABILITY,
        NBT
    }

    public IFilteredUpgrade getFilteredWrapper() {
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

    public FilterLogic getFilterLogic() {
        return this.getFilteredWrapper()
            .getFilterLogic();
    }
}
