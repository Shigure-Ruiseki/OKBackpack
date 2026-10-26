package ruiseki.okstoragecore.upgrades.crafting;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.client.gui.slot.ResizableItemSlot;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okbackpack.client.gui.widget.ShiftButtonWidget;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.StorageSlotGrid;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class CraftingUpgradeTab extends ExpandedUpgradeTab<CraftingUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> SHIFT_CLICK_INTO_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.sophisticatedstorage.upgrades.buttons.shift_click_into_inventory"),
            OSCTextures.SHIFT_CLICK_INTO_INVENTORY),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.sophisticatedstorage.upgrades.buttons.shift_click_into_storage"),
            OSCTextures.SHIFT_CLICK_INTO_STORAGE));

    private static final List<CyclicVariantButtonWidget.Variant> FILL_CRAFTING_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.refill_crafting_grid"),
            OSCTextures.FILL_CRAFTING),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.do_not_refill_crafting_grid"),
            OSCTextures.DO_NOT_FILL_CRAFTING));

    public CraftingUpgradeTab(int slotIndex, CraftingUpgradeWrapper wrapper, ItemStack stack,
        IStorageWrapper storageWrapper, StoragePanel storagePanel, String titleKey) {
        super(slotIndex, wrapper, stack, storageWrapper, storagePanel, titleKey);
    }

    @Override
    protected Flow addUpgradeColumn() {
        return super.addUpgradeColumn().child(this.addButtonRow())
            .child(this.addCraftingRow());
    }

    protected Flow addButtonRow() {
        return Flow.row()
            .coverChildrenHeight()
            .childPadding(2)
            .child(this.addShiftClickIntoButton())
            .child(this.addFillCraftingButton());
    }

    protected CyclicVariantButtonWidget addFillCraftingButton() {
        return new CyclicVariantButtonWidget(
            FILL_CRAFTING_VARIANTS,
            this.getWrapper()
                .shouldRefillCraftingGrid() ? 0 : 1,
            index -> {
                this.getWrapper()
                    .setRefillCraftingGrid(index == 0);
            }).size(20, 20);
    }

    protected CyclicVariantButtonWidget addShiftClickIntoButton() {
        return new CyclicVariantButtonWidget(
            SHIFT_CLICK_INTO_VARIANTS,
            this.getWrapper()
                .shouldShiftClickIntoStorage() ? 0 : 1,
            index -> {
                this.getWrapper()
                    .setShiftClickIntoStorage(index == 0);
            }).size(20, 20);
    }

    protected Flow addCraftingRow() {
        return Flow.row()
            .coverChildren()
            .childPadding(2)
            .child(this.addCraftingGrid())
            .child(this.addCraftingTweakColumn());
    }

    protected StorageSlotGrid<ItemSlot> addCraftingGrid() {
        int slotIndex = this.getSlotIndex();
        StorageSlotGrid<ItemSlot> craftingGrid = new StorageSlotGrid<>().name("crafting_matrix")
            .coverChildren();
        for (int i = 0; i < 9; i++) {
            craftingGrid.child(
                new ItemSlot().syncHandler("crafting_slot_" + slotIndex, i)
                    .pos(i % 3 * 18, i / 3 * 18)
                    .name("crafting_slot_" + i));
        }
        craftingGrid.child(
            new ResizableItemSlot(26).syncHandler("crafting_result_" + slotIndex, 0)
                .pos(14, 63)
                .name("crafting_result_" + slotIndex));
        return craftingGrid;
    }

    protected Flow addCraftingTweakColumn() {
        return Flow.column()
            .coverChildren()
            .childPadding(2)
            .topRel(0)
            .child(this.addRotatedButton())
            .child(this.addGridButton())
            .child(this.addClearButton());
    }

    protected ButtonWidget<?> addRotatedButton() {
        return new ShiftButtonWidget().size(16)
            .setNotShift(OSCTextures.ROTATED_RIGHT)
            .setShift(OSCTextures.ROTATED_LEFT)
            .onMousePressed(button -> {
                if (button != 0) return false;
                Interactable.playButtonClickSound();
                boolean clockwise = !Interactable.hasShiftDown();
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.CRAFTING_ROTATED, buf -> buf.writeBoolean(clockwise));
                return true;
            });
    }

    protected ButtonWidget<?> addGridButton() {
        return new ShiftButtonWidget().size(16)
            .setNotShift(OSCTextures.BALANCE)
            .setShift(OSCTextures.SPREAD)
            .onMousePressed(button -> {
                if (button != 0) return false;
                Interactable.playButtonClickSound();
                boolean balance = !Interactable.hasShiftDown();
                this.getSyncHandler()
                    .syncToServer(
                        balance ? UpgradeSlotSHRegistries.CRAFTING_BALANCE : UpgradeSlotSHRegistries.CRAFTING_SPREAD);
                return true;
            });
    }

    protected ButtonWidget<?> addClearButton() {
        return new ButtonWidget<>().size(16)
            .overlay(OSCTextures.CLEAR)
            .onMousePressed(button -> {
                if (button != 0) return false;
                Interactable.playButtonClickSound();
                this.getSyncHandler()
                    .syncToServer(
                        UpgradeSlotSHRegistries.CRAFTING_CLEAR,
                        buf -> buf.writeBoolean(
                            this.getWrapper()
                                .shouldShiftClickIntoStorage()));
                return true;
            });
    }
}
