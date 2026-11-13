package ruiseki.okstoragecore.upgrades.compacting;

import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okbackpack.client.gui.widget.CyclicVariantButtonWidget;
import ruiseki.okstoragecore.client.gui.OSCTextures;
import ruiseki.okstoragecore.client.gui.sync.UpgradeSlotSHRegistries;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.filter.FilterLogicControl;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class CompactingUpgradeTab extends ExpandedUpgradeTab<CompactingUpgradeWrapper> {

    private static final List<CyclicVariantButtonWidget.Variant> COMPACT_VARIANTS = Arrays.asList(
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.compact_anything"),
            OSCTextures.COMPACT_ANY),
        new CyclicVariantButtonWidget.Variant(
            IKey.lang("gui.okstoragecore.upgrades.buttons.compact_only_uncraftable"),
            OSCTextures.COMPACT_ONLY_UNCRAFTABLE));

    public CompactingUpgradeTab(int slotIndex, CompactingUpgradeWrapper wrapper, ItemStack stack,
        StoragePanel storagePanel, String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    protected CyclicVariantButtonWidget addCompactButton() {
        return new CyclicVariantButtonWidget(
            COMPACT_VARIANTS,
            this.getWrapper()
                .shouldCompactNonUncraftable() ? 0 : 1,
            index -> {
                this.getWrapper()
                    .setCompactNonUncraftable(index == 0);
                this.getSyncHandler()
                    .syncToServer(
                        UpgradeSlotSHRegistries.COMPACT_UNCRAFTABLE,
                        buffer -> buffer.writeBoolean(index == 0));
            }).size(20, 20);
    }

    public static class Basic extends CompactingUpgradeTab {

        public Basic(int slotIndex, CompactingUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.compacting");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new FilterLogicControl.Basic(this.getSlotIndex(), this.getWrapper(), "basic_filter"));
        }
    }

    public static class Advanced extends CompactingUpgradeTab {

        public Advanced(int slotIndex, CompactingUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.advanced_compacting");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn().child(this.addButtonRow())
                .child(new FilterLogicControl.Advanced(this.getSlotIndex(), this.getWrapper(), "adv_filter"));
        }

        protected Flow addButtonRow() {
            return Flow.row()
                .coverChildrenHeight()
                .childPadding(2)
                .child(this.addCompactButton());
        }
    }
}
