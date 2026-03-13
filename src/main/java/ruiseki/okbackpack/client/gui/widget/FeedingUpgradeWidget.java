package ruiseki.okbackpack.client.gui.widget;

import ruiseki.okbackpack.common.init.ModItems;
import ruiseki.okbackpack.common.item.wrapper.FeedingUpgradeWrapper;

public class FeedingUpgradeWidget extends BasicExpandedTabWidget<FeedingUpgradeWrapper> {

    public FeedingUpgradeWidget(int slotIndex, FeedingUpgradeWrapper wrapper) {
        super(
            slotIndex,
            wrapper,
            ModItems.FEEDING_UPGRADE.newItemStack(),
            "gui.backpack.feeding_settings",
            "feeding_filter");
    }
}
