package ruiseki.okstoragecore.upgrades.filter;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.client.gui.OKBGuiTextures;
import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.filter.ContentsFilterControl;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class FilterUpgradeTab extends ExpandedUpgradeTab<FilterUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> DIRECTION_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.direction_both"),
            OKBGuiTextures.IN_OUT_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.direction_input"),
            OKBGuiTextures.IN_ICON),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.direction_output"),
            OKBGuiTextures.OUT_ICON));

    public FilterUpgradeTab(int slotIndex, FilterUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel,
        String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    protected CyclicVariantButtonWidget addDirectionButton() {
        return new CyclicVariantButtonWidget(
            DIRECTION_VARIANTS,
            this.getWrapper()
                .getDirection()
                .ordinal(),
            index -> {
                this.getWrapper()
                    .setDirection(Direction.fromOrdinal(index));
                this.getSyncHandler()
                    .syncToServer(UpgradeSlotSHRegistries.DIRECTION, buffer -> buffer.writeInt(index));
            }).size(20, 20);
    }

    public static class Basic extends FilterUpgradeTab {

        public Basic(int slotIndex, FilterUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.filter");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new ContentsFilterControl.Basic(this.getSlotIndex(), this.getWrapper(), "basic_filter"));
        }
    }

    public static class Advanced extends FilterUpgradeTab {

        public Advanced(int slotIndex, FilterUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.advanced_filter");
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
                .child(this.addDirectionButton());
        }
    }
}
