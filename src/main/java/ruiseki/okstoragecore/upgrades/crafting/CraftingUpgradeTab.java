package ruiseki.okstoragecore.upgrades.crafting;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;

import ruiseki.okbackpack.client.gui.slot.BigItemSlot;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class CraftingUpgradeTab extends ExpandedUpgradeTab<CraftingUpgradeWrapper> {

    private final CraftingUpgradeWrapper wrapper;

    private static final List<CyclicVariantButtonWidget.Variant> FILL_CRAFTING_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.refill_crafting_grid"),
            OSCTextures.FILL_CRAFTING),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.do_not_refill_crafting_grid"),
            OSCTextures.DO_NOT_FILL_CRAFTING));

    public CraftingUpgradeTab(int slotIndex, CraftingUpgradeWrapper wrapper, ItemStack stack,
        IStorageWrapper storageWrapper, StoragePanel panel, String titleKey) {
        super(slotIndex, 5, stack, storageWrapper, panel, titleKey, 90);
        this.wrapper = wrapper;
    }

    @Override
    protected CraftingUpgradeWrapper getWrapper() {
        return this.wrapper;
    }

    @Override
    protected Flow addUpgradeColumn() {
        return super.addUpgradeColumn().child(this.addButtonRow())
            .child(this.addCraftingRow());
    }

    protected Flow addCraftingRow() {
        SlotGroupWidget craftingGroupsWidget = new SlotGroupWidget().name("crafting_matrix")
            .coverChildren();

        for (int i = 0; i < 9; i++) {
            ItemSlot itemSlot = new ItemSlot().syncHandler("crafting_slot_" + slotIndex, i)
                .pos(i % 3 * 18, i / 3 * 18)
                .name("crafting_slot_" + i);
            craftingGroupsWidget.child(itemSlot);
        }

        ItemSlot craftingResult = new BigItemSlot().syncHandler("crafting_result_" + slotIndex, 0)
            .pos(18, 18 * 3 + 9)
            .name("crafting_result_" + slotIndex);
        craftingGroupsWidget.child(craftingResult);

        return Flow.row()
            .coverChildrenHeight()
            .childPadding(2)
            .child(craftingGroupsWidget);
    }

    protected Flow addButtonRow() {

        CyclicVariantButtonWidget fillCraftingButton = new CyclicVariantButtonWidget(
            FILL_CRAFTING_VARIANTS,
            this.getWrapper()
                .shouldRefillCraftingGrid() ? 0 : 1,
            index -> {
                wrapper.setRefillCraftingGrid(index == 0);
                syncToServer();
            }).size(20, 20);

        return Flow.row()
            .coverChildrenHeight()
            .childPadding(2)
            .child(fillCraftingButton);
    }

    public void syncToServer() {
        this.getSyncHandler()
            .syncToServer(UpgradeSlotSHRegistries.CRAFTING_UPGRADE, buf -> {
                buf.writeBoolean(
                    this.getWrapper()
                        .shouldRefillCraftingGrid());
                buf.writeBoolean(
                    this.getWrapper()
                        .shouldShiftClickIntoStorage());
            });
    }
}
