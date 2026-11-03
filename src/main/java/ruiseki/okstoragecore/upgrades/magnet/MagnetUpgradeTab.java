package ruiseki.okstoragecore.upgrades.magnet;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.filter.ContentsFilterControl;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class MagnetUpgradeTab extends ExpandedUpgradeTab<MagnetUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> EXP_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.pickup_xp"),
            OSCTextures.EXP_MAGNET),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.do_not_pickup_xp"),
            OSCTextures.IGNORE_EXP_MAGNET));

    private static final List<CyclicVariantButtonWidget.Variant> ITEM_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.pickup_items"),
            OSCTextures.ITEM_MAGNET),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.do_not_pickup_items"),
            OSCTextures.IGNORE_ITEM_MAGNET));

    public MagnetUpgradeTab(int slotIndex, MagnetUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel,
        String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    protected CyclicVariantButtonWidget addPickupXpButton() {
        return new CyclicVariantButtonWidget(
            EXP_VARIANTS,
            this.getWrapper()
                .shouldPickupXp() ? 0 : 1,
            index -> {
                this.getWrapper()
                    .setPickupXp(index == 0);
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.PICKUP_XP, buffer -> buffer.writeBoolean(index == 0));
            }).tooltip(tooltip -> tooltip.addLine(IKey.lang("gui.okstoragecore.upgrades.buttons.pickup_xp.detail")))
                .size(20, 20);
    }

    protected CyclicVariantButtonWidget addPickupItemsButton() {
        return new CyclicVariantButtonWidget(
            ITEM_VARIANTS,
            this.getWrapper()
                .shouldPickupItems() ? 0 : 1,
            index -> {
                this.getWrapper()
                    .setPickupItems(index == 0);
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.PICKUP_ITEMS, buffer -> buffer.writeBoolean(index == 0));
            }).size(20, 20);
    }

    public static class Basic extends MagnetUpgradeTab {

        public Basic(int slotIndex, MagnetUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.magnet");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new ContentsFilterControl.Basic(this.getSlotIndex(), this.getWrapper(), "basic_filter"));
        }
    }

    public static class Advanced extends MagnetUpgradeTab {

        public Advanced(int slotIndex, MagnetUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.advanced_magnet");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn().child(this.addButtonRow())
                .child(new ContentsFilterControl.Advanced(this.getSlotIndex(), this.getWrapper(), "adv_filter"));
        }

        protected Flow addButtonRow() {
            return Flow.row()
                .coverChildrenHeight()
                .childPadding(2)
                .child(this.addPickupItemsButton())
                .child(this.addPickupXpButton());
        }
    }
}
