package ruiseki.backpack.upgrades.deposit;

import net.minecraft.item.ItemStack;

import com.cleanroommc.modularui.widgets.layout.Flow;

import ruiseki.okstoragecore.client.gui.widget.StoragePanel;
import ruiseki.okstoragecore.client.gui.widget.upgrade.ExpandedUpgradeTab;

public class DepositUpgradeTab extends ExpandedUpgradeTab<DepositUpgradeWrapper> {

    public DepositUpgradeTab(int slotIndex, DepositUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel,
        String titleKey) {
        super(slotIndex, wrapper, stack, storagePanel, titleKey);
    }

    public static class Basic extends DepositUpgradeTab {

        public Basic(int slotIndex, DepositUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.deposit");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new DepositFilterLogicControl.Basic(this.getSlotIndex(), this.getWrapper(), "basic_filter"));
        }
    }

    public static class Advanced extends DepositUpgradeTab {

        public Advanced(int slotIndex, DepositUpgradeWrapper wrapper, ItemStack stack, StoragePanel storagePanel) {
            super(slotIndex, wrapper, stack, storagePanel, "gui.okstoragecore.upgrades.advanced_deposit");
        }

        @Override
        protected Flow addUpgradeColumn() {
            return super.addUpgradeColumn()
                .child(new DepositFilterLogicControl.Advanced(this.getSlotIndex(), this.getWrapper(), "adv_filter"));
        }
    }
}
