package ruiseki.okstoragecore.upgrades.pickup;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.filter.ContentsFilterControl;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class PickupUpgradeTab extends ExpandedUpgradeTab<PickupUpgradeWrapper> {

    public PickupUpgradeTab(int slotIndex, PickupUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel,
        String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    public static class Basic extends PickupUpgradeTab {

        public Basic(int slotIndex, PickupUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.pickup");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new ContentsFilterControl.Basic(this.getSlotIndex(), this.getWrapper(), "basic_filter_"));
        }
    }

    public static class Advanced extends PickupUpgradeTab {

        public Advanced(int slotIndex, PickupUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.advanced_pickup");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new ContentsFilterControl.Advanced(this.getSlotIndex(), this.getWrapper(), "adv_filter"));
        }
    }
}
