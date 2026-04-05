package ruiseki.okbackpack.client.gui.widget.upgrade;

import net.minecraft.item.ItemStack;

import ruiseki.okbackpack.api.IStoragePanel;
import ruiseki.okbackpack.common.item.feeding.FeedingUpgradeWrapper;

public class FeedingUpgradeWidget extends BasicExpandedTabWidget<FeedingUpgradeWrapper> {

    public FeedingUpgradeWidget(int slotIndex, FeedingUpgradeWrapper wrapper, ItemStack stack, IStoragePanel<?> panel,
        String titleKey) {
        super(slotIndex, wrapper, stack, titleKey, "feeding_filter");
    }
}
