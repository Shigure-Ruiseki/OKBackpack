package ruiseki.backpack.upgrades.restock;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.filter.ContentsFilterControl;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class RestockUpgradeTab extends ExpandedUpgradeTab<RestockUpgradeWrapper> {

    public RestockUpgradeTab(int slotIndex, RestockUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel,
        String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    public static class Basic extends RestockUpgradeTab {

        public Basic(int slotIndex, RestockUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.restock");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new ContentsFilterControl.Basic(this.getSlotIndex(), this.getWrapper(), "basic_filter"));
        }
    }

    public static class Advanced extends RestockUpgradeTab {

        public Advanced(int slotIndex, RestockUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.advanced_restock");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new ContentsFilterControl.Advanced(this.getSlotIndex(), this.getWrapper(), "adv_filter"));
        }
    }
}
