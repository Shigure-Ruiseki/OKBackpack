package ruiseki.okstoragecore.upgrades.feeding;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.filter.FilterLogicControl;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class FeedingUpgradeTab extends ExpandedUpgradeTab<FeedingUpgradeWrapper> {

    public FeedingUpgradeTab(int slotIndex, int coveredTabSize, FeedingUpgradeWrapper wrapper, ItemStack stack,
        IStorageWrapper storageWrapper, StoragePanel storagePanel, String titleKey, int width) {
        super(slotIndex, coveredTabSize, wrapper, stack, storageWrapper, storagePanel, titleKey, width);
    }

    public static class Basic extends FeedingUpgradeTab {

        public Basic(int slotIndex, FeedingUpgradeWrapper wrapper, ItemStack stack, IStorageWrapper storageWrapper,
            StoragePanel storagePanel, String titleKey, int width) {
            super(slotIndex, 4, wrapper, stack, storageWrapper, storagePanel, titleKey, 75);
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new FilterLogicControl.Basic(this.getSlotIndex(), this.getWrapper(), "filter"));
        }
    }

    public static class Advanced extends FeedingUpgradeTab {

        public Advanced(int slotIndex, FeedingUpgradeWrapper wrapper, ItemStack stack, IStorageWrapper storageWrapper,
            StoragePanel storagePanel, String titleKey) {
            super(slotIndex, 6, wrapper, stack, storageWrapper, storagePanel, titleKey, 100);
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new FilterLogicControl.Advanced(this.getSlotIndex(), this.getWrapper(), "adv_filter"));
        }
    }
}
